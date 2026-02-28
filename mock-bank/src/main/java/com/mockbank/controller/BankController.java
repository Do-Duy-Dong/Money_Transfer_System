package com.mockbank.controller;

import com.mockbank.dto.*;
import com.mockbank.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping("/link")
    public ResponseEntity<LinkResponse> link(@RequestBody @Valid LinkRequest request) {
        return new ResponseEntity<>(bankService.linkPartner(request), HttpStatus.CREATED);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader("X-Partner-Token") String token,
            @RequestHeader("X-Signature") String signature,
            @RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.ok(bankService.processDepositByWallet(token, signature, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader("X-Partner-Token") String token,
            @RequestHeader("X-Signature") String signature,
            @RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.ok(bankService.processWithdrawByWallet(token, signature, request));
    }
//    Viết thêm endpoint callback
    @PostMapping("/callback")
    public ResponseEntity<TransactionResponse> callback(
            @RequestBody @Valid CallBackRequest request) {
        return ResponseEntity.ok(bankService.pendingTransaction(request)); }
}
