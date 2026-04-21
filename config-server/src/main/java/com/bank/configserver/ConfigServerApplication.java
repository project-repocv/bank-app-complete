package com.bank.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application - Centralized Configuration Management for the Banking System.
 * 
 * This service provides centralized configuration management for all microservices.
 * It uses a Git backend to store configuration files, enabling version control and
 * easy configuration updates without redeploying services.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
