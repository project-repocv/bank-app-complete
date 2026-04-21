package com.bank.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Customer Service Application - Manages customer profiles for the Banking System.
 * 
 * This service handles:
 * - Customer registration and profile management
 * - KYC (Know Your Customer) status tracking
 * - Customer data encryption for sensitive fields
 * - Integration with other microservices via Feign clients
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
