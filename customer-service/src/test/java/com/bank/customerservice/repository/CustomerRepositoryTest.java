package com.bank.customerservice.repository;

import com.bank.customerservice.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class CustomerRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldSaveAndFindCustomerById() {
        // Given
        Customer customer = Customer.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        // When
        Customer savedCustomer = customerRepository.save(customer);
        Optional<Customer> foundCustomer = customerRepository.findByCustomerId(savedCustomer.getCustomerId());

        // Then
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getFullName()).isEqualTo("John Doe");
        assertThat(foundCustomer.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindByEmail() {
        // Given
        Customer customer = Customer.builder()
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .phone("+0987654321")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .build();

        // When
        customerRepository.save(customer);
        Optional<Customer> foundCustomer = customerRepository.findByEmail("jane.smith@example.com");

        // Then
        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() {
        // When
        Optional<Customer> customer = customerRepository.findByCustomerId("non-existent-id");

        // Then
        assertThat(customer).isEmpty();
    }
}
