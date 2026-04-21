package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKey(String key);
    boolean existsByKey(String key);
    void deleteByExpiresAtBefore(LocalDateTime expiryDate);
}
