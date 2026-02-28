package com.mockbank.repository;

import com.mockbank.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByTokenAndRequestIdAndType(String token, String requestId, String type);
}
