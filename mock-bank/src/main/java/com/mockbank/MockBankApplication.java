package com.mockbank;

import com.mockbank.entity.BankLink;
import com.mockbank.repository.BankLinkRepository;
import com.mockbank.repository.BankTransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class MockBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockBankApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner demo(com.mockbank.repository.BankAccountRepository repository) {
        return (args) -> {
            if (repository.count() == 0) {
                repository.save(com.mockbank.entity.BankAccount.builder()
                        .accountNo("10000001")
                        .ownerName("User One")
                        .balance(1000000L)
                        .build());
                repository.save(com.mockbank.entity.BankAccount.builder()
                        .accountNo("10000002")
                        .ownerName("User Two")
                        .balance(200000L)
                        .build());

            }
        };
    }

    @Bean
    public CommandLineRunner demoLink(
            BankLinkRepository bankLinkRepository,
            BankTransactionRepository bankTransactionRepository) {
        return (args) -> {
            if (bankLinkRepository.count() == 0) {
                bankLinkRepository.save(BankLink.builder()
                        .partnerId("ACC002")
                        .secretKey("6583d2d3d1eb4e67a1143d1fe3f7f053")
                        .token("b9cd9a29-f33c-409e-8e32-b68a0fb30786")
                        .bankNo("10000001")
                        .status("LINKED")
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            if(bankTransactionRepository.count() == 0) {
                bankTransactionRepository.save(com.mockbank.entity.BankTransaction.builder()
                        .token("b9cd9a29-f33c-409e-8e32-b68a0fb30786")
                        .requestId("nvcv123")
                        .type("DEPOSIT")
                        .accountNo("10000001")
                        .amount(1000L)
                        .status("SUCCESS")
                        .message("Initial deposit transaction")
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        };
    }

}
