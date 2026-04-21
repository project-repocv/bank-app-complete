package com.bank.customerservice.service;

import com.bank.customerservice.dto.CustomerRequest;
import com.bank.customerservice.dto.CustomerResponse;
import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.entity.KycStatus;
import com.bank.customerservice.exception.CustomerAlreadyExistsException;
import com.bank.customerservice.exception.CustomerNotFoundException;
import com.bank.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());
        
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Customer with email {} already exists", request.getEmail());
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }
        
        try {
            Customer customer = mapper.toEntity(request);
            customer.setKycStatus(KycStatus.PENDING); // Default KYC status
            
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Successfully created customer with ID: {}", savedCustomer.getCustomerId());
            
            return mapper.toResponse(savedCustomer);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create customer due to data integrity violation: {}", e.getMessage());
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }
    }
    
    public CustomerResponse getCustomerById(String customerId) {
        log.debug("Fetching customer with ID: {}", customerId);
        
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found"));
        
        return mapper.toResponse(customer);
    }
    
    public CustomerResponse getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with email " + email + " not found"));
        
        return mapper.toResponse(customer);
    }
    
    @Transactional
    public CustomerResponse updateCustomer(String customerId, CustomerRequest request) {
        log.info("Updating customer with ID: {}", customerId);
        
        Customer existingCustomer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found"));
        
        // Check if email is being updated and if new email already exists
        if (!existingCustomer.getEmail().equals(request.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("Customer with email " + request.getEmail() + " already exists");
        }
        
        mapper.partialUpdate(request, existingCustomer);
        existingCustomer.setUpdatedAt(java.time.LocalDateTime.now());
        
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Successfully updated customer with ID: {}", customerId);
        
        return mapper.toResponse(updatedCustomer);
    }
    
    @Transactional
    public CustomerResponse updateKycStatus(String customerId, KycStatus kycStatus) {
        log.info("Updating KYC status for customer ID: {} to: {}", customerId, kycStatus);
        
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found"));
        
        customer.setKycStatus(kycStatus);
        customer.setUpdatedAt(java.time.LocalDateTime.now());
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Successfully updated KYC status for customer ID: {}", customerId);
        
        return mapper.toResponse(updatedCustomer);
    }
    
    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
