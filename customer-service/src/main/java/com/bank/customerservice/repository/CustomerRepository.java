package com.bank.customerservice.repository;

import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.entity.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Customer Repository - Data access layer for Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(UUID customerId);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(value = jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId")
    Optional<Customer> findByIdempotentLock(UUID customerId);
}
