package com.mockbank.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionResponse {
    private String requestId;
    private int status;
    private String bankNo;
    private String walletNo;
    private Long amount;
    private String type;
}
