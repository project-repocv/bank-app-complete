package com.bank.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", fallback = CustomerServiceFallback.class)
public interface CustomerServiceClient {
    
    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable("id") String customerId);
}
