package com.bank.customerservice.dto;

import org.mapstruct.*;
import com.bank.customerservice.entity.Customer;
import com.bank.customerservice.entity.KycStatus;

/**
 * MapStruct Mapper for converting between Customer entity and DTOs.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerRequest request, @MappingTarget Customer customer);
}
