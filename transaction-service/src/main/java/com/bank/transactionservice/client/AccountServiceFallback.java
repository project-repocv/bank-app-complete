package com.bank.transactionservice.client;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AccountServiceFallback implements AccountServiceClient {

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        throw new RuntimeException("Account service is currently unavailable");
    }

    @Override
    public AccountResponse debitAccount(String accountNumber, DebitRequest request) {
        throw new RuntimeException("Account service is currently unavailable");
    }

    @Override
    public AccountResponse creditAccount(String accountNumber, CreditRequest request) {
        throw new RuntimeException("Account service is currently unavailable");
    }
}
