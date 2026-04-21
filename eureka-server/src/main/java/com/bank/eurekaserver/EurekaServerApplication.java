package com.bank.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application - Service Registry for the Banking System.
 * 
 * This service enables service discovery for all microservices in the banking system.
 * Each microservice registers itself with Eureka upon startup and can discover other
 * services through this registry.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
