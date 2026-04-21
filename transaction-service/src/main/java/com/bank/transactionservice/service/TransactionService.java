package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountServiceClient;
import com.bank.transactionservice.dto.TransactionRequest;
import com.bank.transactionservice.dto.TransactionResponse;
import com.bank.transactionservice.entity.*;
import com.bank.transactionservice.exception.DuplicateTransactionException;
import com.bank.transactionservice.exception.InsufficientFundsException;
import com.bank.transactionservice.exception.TransactionNotFoundException;
import com.bank.transactionservice.repository.IdempotencyKeyRepository;
import com.bank.transactionservice.repository.TransactionRepository;
import com.bank.transactionservice.util.NotificationMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AccountServiceClient accountServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction with idempotency key: {}", request.getIdempotencyKey());

        if (idempotencyKeyRepository.existsByKey(request.getIdempotencyKey())) {
            log.warn("Duplicate transaction attempt with idempotency key: {}", request.getIdempotencyKey());
            throw new DuplicateTransactionException("Transaction with this idempotency key already exists");
        }

        Transaction transaction = Transaction.builder()
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(request.getTransactionType())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .sagaId(UUID.randomUUID().toString())
                .build();

        try {
            switch (request.getTransactionType()) {
                case DEPOSIT -> processDeposit(transaction, request);
                case WITHDRAWAL -> processWithdrawal(transaction, request);
                case TRANSFER_OUT -> processTransfer(transaction, request);
                default -> throw new IllegalArgumentException("Unsupported transaction type: " + request.getTransactionType());
            }

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());

            saveIdempotencyKey(request.getIdempotencyKey(), transaction.getTransactionId(), "COMPLETED");

            sendNotification(transaction);

        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            saveIdempotencyKey(request.getIdempotencyKey(), transaction.getTransactionId(), "FAILED");
            throw e;
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    private void processDeposit(Transaction transaction, TransactionRequest request) {
        log.info("Processing deposit for account: {}", transaction.getSourceAccountNumber());
        
        AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                request.getAmount(),
                request.getCurrency(),
                request.getIdempotencyKey(),
                request.getDescription()
        );
        
        accountServiceClient.creditAccount(transaction.getSourceAccountNumber(), creditRequest);
    }

    private void processWithdrawal(Transaction transaction, TransactionRequest request) {
        log.info("Processing withdrawal for account: {}", transaction.getSourceAccountNumber());
        
        AccountServiceClient.DebitRequest debitRequest = new AccountServiceClient.DebitRequest(
                request.getAmount(),
                request.getCurrency(),
                request.getIdempotencyKey(),
                request.getDescription()
        );
        
        accountServiceClient.debitAccount(transaction.getSourceAccountNumber(), debitRequest);
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "processTransferFallback")
    private void processTransfer(Transaction transaction, TransactionRequest request) throws Exception {
        log.info("Processing transfer from {} to {}", 
                transaction.getSourceAccountNumber(), 
                transaction.getDestinationAccountNumber());

        String sagaId = transaction.getSagaId();
        boolean debitSuccess = false;

        try {
            AccountServiceClient.DebitRequest debitRequest = new AccountServiceClient.DebitRequest(
                    request.getAmount(),
                    request.getCurrency(),
                    request.getIdempotencyKey() + "-debit",
                    "Transfer out to " + transaction.getDestinationAccountNumber()
            );
            
            accountServiceClient.debitAccount(transaction.getSourceAccountNumber(), debitRequest);
            debitSuccess = true;

            AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                    request.getAmount(),
                    request.getCurrency(),
                    request.getIdempotencyKey() + "-credit",
                    "Transfer in from " + transaction.getSourceAccountNumber()
            );
            
            accountServiceClient.creditAccount(transaction.getDestinationAccountNumber(), creditRequest);

        } catch (Exception e) {
            if (debitSuccess) {
                log.error("Transfer failed after debit, initiating compensation for saga: {}", sagaId);
                compensateTransfer(transaction, request);
            }
            throw e;
        }
    }

    private void processTransferFallback(Exception e) {
        log.error("Circuit breaker opened for account service during transfer: {}", e.getMessage());
        throw new RuntimeException("Account service unavailable, transfer cannot be processed");
    }

    private void compensateTransfer(Transaction transaction, TransactionRequest request) {
        log.info("Compensating transfer for saga: {}", transaction.getSagaId());
        
        try {
            AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                    request.getAmount(),
                    request.getCurrency(),
                    UUID.randomUUID().toString(),
                    "Compensation for failed transfer"
            );
            
            accountServiceClient.creditAccount(transaction.getSourceAccountNumber(), creditRequest);
            transaction.setStatus(TransactionStatus.COMPENSATED);
        } catch (Exception e) {
            log.error("Compensation failed for saga {}: {}", transaction.getSagaId(), e.getMessage());
        }
    }

    private void saveIdempotencyKey(String key, String transactionId, String status) {
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .key(key)
                .transactionId(transactionId)
                .status(status)
                .build();
        idempotencyKeyRepository.save(idempotencyKey);
    }

    private void sendNotification(Transaction transaction) {
        try {
            NotificationMessage message = NotificationMessage.builder()
                    .transactionId(transaction.getTransactionId())
                    .accountNumber(transaction.getSourceAccountNumber())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .type(transaction.getTransactionType())
                    .status(transaction.getStatus())
                    .timestamp(transaction.getCreatedAt())
                    .build();

            rabbitTemplate.convertAndSend(
                    com.bank.transactionservice.config.RabbitMQConfig.TRANSACTION_EXCHANGE,
                    com.bank.transactionservice.config.RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                    message
            );
            log.info("Notification sent for transaction: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to send notification for transaction {}: {}", 
                    transaction.getTransactionId(), e.getMessage());
        }
    }

    public TransactionResponse getTransactionById(String transactionId) {
        log.debug("Fetching transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with ID " + transactionId + " not found"));
        
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {
        log.debug("Fetching transactions for account: {}", accountNumber);
        
        List<Transaction> transactions = transactionRepository.findBySourceAccountNumber(accountNumber);
        return transactions.stream().map(this::mapToResponse).toList();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}
