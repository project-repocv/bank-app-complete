package com.bank.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Account type is required")
    private String accountType;
    
    private String currency;
}
