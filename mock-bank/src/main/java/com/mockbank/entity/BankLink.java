package com.mockbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_links")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerId;

    @Column(unique = true, nullable = false)
    private String token;

    private String secretKey;

    private String status; // LINKED, INACTIVE

    private String bankNo;
    private LocalDateTime createdAt;
}
