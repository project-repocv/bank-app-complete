package com.bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "account-service", fallback = AccountServiceFallback.class)
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);

    @PutMapping("/api/accounts/{accountNumber}/debit")
    AccountResponse debitAccount(@PathVariable String accountNumber, @RequestBody DebitRequest request);

    @PutMapping("/api/accounts/{accountNumber}/credit")
    AccountResponse creditAccount(@PathVariable String accountNumber, @RequestBody CreditRequest request);

    record AccountResponse(String accountNumber, String customerId, String type, BigDecimal balance, String currency, String status) {}
    record DebitRequest(BigDecimal amount, String currency, String idempotencyKey, String description) {}
    record CreditRequest(BigDecimal amount, String currency, String idempotencyKey, String description) {}
}
