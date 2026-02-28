package com.example.user_service.dto;

import com.example.user_service.model.Account;

public interface TransactionRule {
    void validate(ContextInput contextInput);

}
