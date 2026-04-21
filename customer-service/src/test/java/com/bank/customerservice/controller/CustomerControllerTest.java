package com.bank.customerservice.controller;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.entity.KycStatus;
import com.bank.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomerSuccessfully() throws Exception {
        // Given
        CustomerRequest request = CustomerRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .customerId("uuid-123")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.PENDING)
                .build();

        given(customerService.createCustomer(any(CustomerRequest.class))).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "John Doe",
                                    "email": "john.doe@example.com",
                                    "phone": "+1234567890",
                                    "dateOfBirth": "1990-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.kycStatus", is("PENDING")));
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() throws Exception {
        // Given
        String customerId = "uuid-123";
        CustomerResponse response = CustomerResponse.builder()
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED)
                .build();

        given(customerService.getCustomerById(customerId)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(customerId)))
                .andExpect(jsonPath("$.kycStatus", is("VERIFIED")));
    }

    @Test
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Given
        String customerId = "uuid-123";
        CustomerRequest request = CustomerRequest.builder()
                .fullName("Updated Name")
                .email("updated.email@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .customerId(customerId)
                .fullName("Updated Name")
                .email("updated.email@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.PENDING)
                .build();

        given(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class))).willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Updated Name",
                                    "email": "updated.email@example.com",
                                    "phone": "+1234567890",
                                    "dateOfBirth": "1990-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Updated Name")));
    }

    @Test
    void shouldUpdateKycStatusSuccessfully() throws Exception {
        // Given
        String customerId = "uuid-123";
        CustomerResponse response = CustomerResponse.builder()
                .customerId(customerId)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KycStatus.VERIFIED)
                .build();

        given(customerService.updateKycStatus(customerId, KycStatus.VERIFIED)).willReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/customers/" + customerId + "/kyc-status?kycStatus=VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus", is("VERIFIED")));
    }

    @Test
    void shouldReturnAllCustomers() throws Exception {
        // Given
        CustomerResponse customer1 = CustomerResponse.builder()
                .customerId("uuid-1")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .kycStatus(KycStatus.PENDING)
                .build();

        CustomerResponse customer2 = CustomerResponse.builder()
                .customerId("uuid-2")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .kycStatus(KycStatus.VERIFIED)
                .build();

        given(customerService.getAllCustomers()).willReturn(List.of(customer1, customer2));

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fullName", is("John Doe")))
                .andExpect(jsonPath("$[1].fullName", is("Jane Smith")));
    }
}
