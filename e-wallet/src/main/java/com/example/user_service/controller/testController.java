package com.example.user_service.controller;

import com.example.user_service.model.Transaction;
import com.example.user_service.repository.TransactionRepository;
import com.example.user_service.service.JobService;
import com.example.user_service.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class testController {
    private final JobService jobService;
    private final TransactionRepository transactionRepository;
    private final StatService statService;
    @PostMapping("/testapi")
    public ResponseEntity<?> test(
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        statService.statDailySaveAll(userDetails.getUsername(),  java.time.LocalDateTime.now());
        return ResponseEntity.ok().build();
    }
}
