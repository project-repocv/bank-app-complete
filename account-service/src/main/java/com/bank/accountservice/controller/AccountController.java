package com.bank.accountservice.controller;

import com.bank.accountservice.dto.AccountRequest;
import com.bank.accountservice.dto.AccountResponse;
import com.bank.accountservice.entity.AccountStatus;
import com.bank.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Operations related to account management")
public class AccountController {
    
    private final AccountService accountService;
    
    @Operation(summary = "Create a new account")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get account by account number")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get accounts by customer ID")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable String customerId) {
        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update account status")
    @PatchMapping("/{accountNumber}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable String accountNumber,
            @RequestParam AccountStatus status) {
        AccountResponse response = accountService.updateAccountStatus(accountNumber, status);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Credit an account (deposit)")
    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponse response = accountService.creditAccount(accountNumber, amount);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Debit an account (withdrawal)")
    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponse response = accountService.debitAccount(accountNumber, amount);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Freeze an account")
    @PostMapping("/{accountNumber}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.freezeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Close an account")
    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.closeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get all accounts")
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }
}
