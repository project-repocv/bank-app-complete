package com.bank.accountservice.client;

import org.springframework.stereotype.Component;

@Component
public class CustomerServiceFallback implements CustomerServiceClient {
    
    @Override
    public CustomerResponse getCustomerById(String customerId) {
        // Return a default response or throw an exception based on your requirements
        return CustomerResponse.builder()
                .customerId(customerId)
                .fullName("Unknown Customer")
                .email("unknown@example.com")
                .phone("")
                .address("")
                .kycStatus("UNKNOWN")
                .build();
    }
}
