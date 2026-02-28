package com.mockbank.repository;

import com.mockbank.entity.BankLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankLinkRepository extends JpaRepository<BankLink, Long> {
    Optional<BankLink> findByToken(String token);
}
