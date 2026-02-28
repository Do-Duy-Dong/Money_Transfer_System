package com.mockbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "token", "requestId", "type" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private String requestId;
    private String type; // DEPOSIT, WITHDRAW
    private String accountNo;
    private Long amount;
    private String status; // SUCCESS, FAILED
    private String message;
    private LocalDateTime createdAt;
}
