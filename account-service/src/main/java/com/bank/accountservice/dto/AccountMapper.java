package com.bank.accountservice.dto;

import org.mapstruct.*;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.entity.AccountType;
import com.bank.accountservice.entity.AccountStatus;

/**
 * MapStruct Mapper for converting between Account entity and DTOs.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AccountMapper {

    Account toEntity(AccountRequest request);

    AccountResponse toResponse(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account partialUpdate(AccountRequest request, @MappingTarget Account account);
    
    default AccountType stringToAccountType(String value) {
        if (value == null) {
            return null;
        }
        return AccountType.valueOf(value.toUpperCase());
    }
    
    default String accountTypeToString(AccountType accountType) {
        if (accountType == null) {
            return null;
        }
        return accountType.name();
    }
    
    default String accountStatusToString(AccountStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }
}
