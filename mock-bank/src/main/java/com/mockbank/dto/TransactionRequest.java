package com.mockbank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotBlank(message = "requestId is required")
    private String requestId;

    @NotBlank(message = "accountNo is required")
    private String bankNo;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Long amount;

    private String walletNo;
    private Long timestamp;
}
