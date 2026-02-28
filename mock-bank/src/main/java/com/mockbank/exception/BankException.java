package com.mockbank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BankException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public BankException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
