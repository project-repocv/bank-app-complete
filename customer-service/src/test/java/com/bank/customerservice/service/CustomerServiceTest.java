package com.bank.customerservice.service;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.entity.KycStatus;
import com.bank.customerservice.exception.CustomerAlreadyExistsException;
import com.bank.customerservice.exception.CustomerNotFoundException;
import com.bank.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, customerMapper);
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .customerId("uuid-123")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.PENDING)
                .build();

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .customerId("uuid-123")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.PENDING)
                .build();

        given(customerRepository.existsByEmail("john.doe@example.com")).willReturn(false);
        given(customerMapper.toEntity(request)).willReturn(customer);
        given(customerRepository.save(any(Customer.class))).willReturn(customer);
        given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

        // When
        CustomerResponse response = customerService.createCustomer(request);

        // Then
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        then(customerRepository).should(times(1)).existsByEmail("john.doe@example.com");
        then(customerRepository).should(times(1)).save(any(Customer.class));
    }

    @Test
    void shouldThrowCustomerAlreadyExistsExceptionWhenEmailExists() {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        given(customerRepository.existsByEmail("john.doe@example.com")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() {
        // Given
        String customerId = "uuid-123";
        Customer customer = Customer.builder()
                .id(1L)
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED)
                .build();

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED)
                .build();

        given(customerRepository.findByCustomerId(customerId)).willReturn(Optional.of(customer));
        given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

        // When
        CustomerResponse response = customerService.getCustomerById(customerId);

        // Then
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        // Given
        String customerId = "non-existent-id";
        given(customerRepository.findByCustomerId(customerId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldUpdateKycStatusSuccessfully() {
        // Given
        String customerId = "uuid-123";
        Customer existingCustomer = Customer.builder()
                .id(1L)
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.PENDING)
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED) // Updated status
                .build();

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED)
                .build();

        given(customerRepository.findByCustomerId(customerId)).willReturn(Optional.of(existingCustomer));
        given(customerRepository.save(any(Customer.class))).willReturn(updatedCustomer);
        given(customerMapper.toResponse(updatedCustomer)).willReturn(expectedResponse);

        // When
        CustomerResponse response = customerService.updateKycStatus(customerId, KycStatus.VERIFIED);

        // Then
        assertThat(response.getKycStatus()).isEqualTo(KycStatus.VERIFIED);
        then(customerRepository).should(times(1)).save(any(Customer.class));
    }
}
