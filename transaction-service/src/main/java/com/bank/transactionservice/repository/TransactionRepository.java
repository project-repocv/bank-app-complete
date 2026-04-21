package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findBySourceAccountNumber(String accountNumber);
    List<Transaction> findByDestinationAccountNumber(String accountNumber);
    List<Transaction> findByStatus(TransactionStatus status);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
