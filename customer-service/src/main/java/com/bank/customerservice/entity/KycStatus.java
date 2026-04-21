package com.bank.customerservice.entity;

/**
 * KYC (Know Your Customer) Status Enum.
 */
public enum KycStatus {
    PENDING,      // KYC verification pending
    VERIFIED,     // KYC verified successfully
    REJECTED,     // KYC verification rejected
    EXPIRED       // KYC has expired and needs renewal
}
