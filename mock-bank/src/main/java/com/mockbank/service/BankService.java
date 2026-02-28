package com.mockbank.service;

import com.mockbank.dto.*;
import com.mockbank.entity.BankAccount;
import com.mockbank.entity.BankLink;
import com.mockbank.entity.BankTransaction;
import com.mockbank.exception.BankException;
import com.mockbank.repository.BankAccountRepository;
import com.mockbank.repository.BankLinkRepository;
import com.mockbank.repository.BankTransactionRepository;
import com.mockbank.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankService {

        private final BankAccountRepository accountRepository;
        private final BankLinkRepository linkRepository;
        private final BankTransactionRepository transactionRepository;

        @Transactional
        public LinkResponse linkPartner(LinkRequest request) {
                String token = UUID.randomUUID().toString();
                String secretKey = UUID.randomUUID().toString().replace("-", "");

                BankLink link = BankLink.builder()
                                .partnerId(request.getPartnerId())
                                .token(token)
                                .secretKey(secretKey)
                                .status("LINKED")
                                .createdAt(LocalDateTime.now())
                                .build();

                linkRepository.save(link);
                log.info("Partner linked: {}", request.getPartnerId());

                return LinkResponse.builder()
                                .status("LINKED")
                                .partnerId(request.getPartnerId())
                                .token(token)
                                .secretKey(secretKey)
                                .createdAt(LocalDateTime.now().toString())
                                .build();
        }

        @Transactional
        public TransactionResponse processDepositByWallet(String token, String signature, TransactionRequest request) {
                return processTransaction(token, signature, request, "DEPOSIT");
        }

        @Transactional
        public TransactionResponse processWithdrawByWallet(String token, String signature, TransactionRequest request) {
                return processTransaction(token, signature, request, "WITHDRAW");
        }

        private TransactionResponse processTransaction(String token, String signature, TransactionRequest request,
                        String type) {
                // 1. Validate Token
                BankLink link = linkRepository.findByToken(token)
                                .orElseThrow(() -> new BankException(HttpStatus.UNAUTHORIZED, "Invalid token"));

                // 2. Validate Signature
                String signingString = request.getRequestId() +request.getWalletNo()+ request.getBankNo() + request.getAmount();
                String expectedSignature = SignatureUtil.calculateSignature(link.getSecretKey(), signingString);

                if (!expectedSignature.equalsIgnoreCase(signature)) {
                        log.warn("Signature mismatch for requestId: {}. Expected: {}, Actual: {}",
                                        request.getRequestId(),
                                        expectedSignature, signature);
                        throw new BankException(HttpStatus.UNAUTHORIZED, "Invalid signature");
                }

                // 3. Idempotency Check
                Optional<BankTransaction> existingTx = transactionRepository.findByTokenAndRequestIdAndType(token,
                                request.getRequestId(), type);
                if (existingTx.isPresent()) {
                        BankTransaction tx = existingTx.get();
                        BankAccount account = accountRepository.findByAccountNo(tx.getAccountNo()).orElseThrow();
                        return TransactionResponse.builder()
                                        .requestId(tx.getRequestId())
                                        .status(1)
                                        .build();
                }
                // 4. Validate Account
                BankAccount account = accountRepository.findByAccountNo(request.getBankNo())
                                .orElseThrow(() -> new BankException(HttpStatus.NOT_FOUND, "Account not found"));

                // 5. Business Logic
                if ("DEPOSIT".equals(type)) {
                        account.setBalance(account.getBalance() + request.getAmount());
                } else if ("WITHDRAW".equals(type)) {
                        if (account.getBalance().compareTo(request.getAmount()) < 0) {
                                throw new BankException(HttpStatus.BAD_REQUEST, "Insufficient balance");
                        }
                        account.setBalance(account.getBalance() - request.getAmount());
                }

                accountRepository.save(account);

                BankTransaction tx = BankTransaction.builder()
                                .token(token)
                                .requestId(request.getRequestId())
                                .type(type)
                                .accountNo(request.getBankNo())
                                .amount(request.getAmount())
                                .status("SUCCESS")
                                .message("Success")
                                .createdAt(LocalDateTime.now())
                                .build();

                transactionRepository.save(tx);
                log.info("{} success for account: {}, amount: {}", type, request.getBankNo(), request.getAmount());

                return TransactionResponse.builder()
                                .requestId(tx.getRequestId())
                        .status(1)
                        .bankNo(request.getBankNo())
                        .walletNo(request.getWalletNo())
                        .amount(request.getAmount())
                        .type(type)
                                .build();
        }
        public TransactionResponse pendingTransaction(
                CallBackRequest request

        ){
                Optional<BankTransaction> tx = transactionRepository.findByTokenAndRequestIdAndType(
                                request.getToken(),
                                request.getRequestId(),
                                request.getType()
                        );
                if(tx.isPresent()){
                        System.out.println("succeed");
                        BankLink wallet= linkRepository.findByToken(request.getToken())
                                .orElseThrow(()->new BankException(HttpStatus.UNAUTHORIZED, "Invalid token"));
                        return TransactionResponse.builder()
                                .requestId(tx.get().getRequestId())
                                .bankNo(tx.get().getAccountNo())
                                .walletNo(request.getWalletNo())
                                .type(request.getType())
                                .amount(tx.get().getAmount())
                                .status(1)
                                .build();
                }else{
                        System.out.println("pending");
                        return TransactionResponse.builder()
                                .requestId(tx.get().getRequestId())
                                .bankNo(tx.get().getAccountNo())
                                .walletNo(request.getWalletNo())
                                .type(request.getType())
                                .amount(tx.get().getAmount())
                                .status(0)
                                .build();
                }
        }
}
