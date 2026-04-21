package com.bank.accountservice.service;

import com.bank.accountservice.client.CustomerResponse;
import com.bank.accountservice.client.CustomerServiceClient;
import com.bank.accountservice.dto.AccountRequest;
import com.bank.accountservice.dto.AccountMapper;
import com.bank.accountservice.dto.AccountResponse;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.entity.AccountStatus;
import com.bank.accountservice.exception.AccountNotFoundException;
import com.bank.accountservice.exception.CustomerNotFoundException;
import com.bank.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountMapper mapper;
    private final CustomerServiceClient customerServiceClient;
    
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating account for customer: {}", request.getCustomerId());
        
        // Validate customer exists by calling Customer Service
        try {
            CustomerResponse customer = customerServiceClient.getCustomerById(request.getCustomerId());
            if (customer == null || "Unknown Customer".equals(customer.getFullName())) {
                log.warn("Customer not found: {}", request.getCustomerId());
                throw new CustomerNotFoundException("Customer with ID " + request.getCustomerId() + " not found");
            }
        } catch (Exception e) {
            log.error("Failed to validate customer: {}", e.getMessage());
            throw new CustomerNotFoundException("Customer with ID " + request.getCustomerId() + " not found or service unavailable");
        }
        
        Account account = mapper.toEntity(request);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        
        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created account: {}", savedAccount.getAccountNumber());
        
        return mapper.toResponse(savedAccount);
    }
    
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        log.debug("Fetching account: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        return mapper.toResponse(account);
    }
    
    public List<AccountResponse> getAccountsByCustomerId(String customerId) {
        log.debug("Fetching accounts for customer: {}", customerId);
        
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    @Transactional
    public AccountResponse updateAccountStatus(String accountNumber, AccountStatus status) {
        log.info("Updating status for account: {} to: {}", accountNumber, status);
        
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);
        log.info("Successfully updated account status: {}", accountNumber);
        
        return mapper.toResponse(updatedAccount);
    }
    
    @Transactional
    public AccountResponse creditAccount(String accountNumber, BigDecimal amount) {
        log.info("Crediting account: {} with amount: {}", accountNumber, amount);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot credit inactive account: " + account.getStatus());
        }
        
        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);
        log.info("Successfully credited account: {}. New balance: {}", accountNumber, updatedAccount.getBalance());
        
        return mapper.toResponse(updatedAccount);
    }
    
    @Transactional
    public AccountResponse debitAccount(String accountNumber, BigDecimal amount) {
        log.info("Debiting account: {} with amount: {}", accountNumber, amount);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot debit inactive account: " + account.getStatus());
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in account: " + accountNumber);
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);
        log.info("Successfully debited account: {}. New balance: {}", accountNumber, updatedAccount.getBalance());
        
        return mapper.toResponse(updatedAccount);
    }
    
    @Transactional
    public AccountResponse freezeAccount(String accountNumber) {
        return updateAccountStatus(accountNumber, AccountStatus.FROZEN);
    }
    
    @Transactional
    public AccountResponse closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with number " + accountNumber + " not found"));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + accountNumber);
        }
        
        return updateAccountStatus(accountNumber, AccountStatus.CLOSED);
    }
    
    public List<AccountResponse> getAllAccounts() {
        log.debug("Fetching all accounts");
        
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
