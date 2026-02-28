package com.example.user_service.service;

import com.example.user_service.dto.BankCallBackResponse;
import com.example.user_service.model.LinkedBank;
import com.example.user_service.model.StatusEnum;
import com.example.user_service.model.Transaction;
import com.example.user_service.repository.AccountRepository;
import com.example.user_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobService {
    @Value("${banking.api.url}")
    private String url;
    private final AccountRepository accountRepository;
    private final LinkedBankService linkedBankService;
    private final TransactionRepository transactionRepository;
    private final StatService statService;
    private final JobScheduler jobScheduler;
    private final RestTemplate restTemplate;

    @Job(name = "Job Auto Retry Pending Transaction", retries=2)
    public void jobAutoRetryPendingTransaction(){
        List<Transaction> pendingTs= transactionRepository.findAllByStatus(100);
        if(pendingTs.isEmpty()){
            return;
        }
        for(Transaction ts : pendingTs){
            jobScheduler.enqueue(()->callMockBank(ts));
        }
    }
    @Job(name = "Job Human Retry Pending Transaction", retries=2)
    public void jobHumanRetryPendingTransaction(String requestId){
        Transaction ts= transactionRepository.findByRequestId(requestId)
                .orElseThrow(()-> new RuntimeException("Transaction not found"));
        if(ts.getStatus().equals(StatusEnum.PENDING)){
            callMockBank(ts);
        }
    }
    @Job(name = "Job Reset Daily Limit Transaction", retries=2)
    public void jobResetDailyLimitTransaction(){

        System.out.println("scan");
    }

//    @Job(name = "Job Stat Transaction Per Day", retries=2)
//    public void jobStatTransactionPerDay(){
//        statService.statDaily()
//    }
    public void callMockBank(Transaction ts){
        LinkedBank linkedBank=linkedBankService.getLinkBankByAccountNumberAndAccount(ts.getLinkedBank().getId(),ts.getFromAccount());
        Map<String,Object> body= Map.of(
                "requestId",ts.getRequestId(),
                "token",linkedBank.getToken(),
                "type",ts.getType(),
                "walletNo", linkedBank.getAccount().getAccountNumber()
        );
        HttpEntity<Map<String,Object>> request= new HttpEntity<>(body);
        ResponseEntity<BankCallBackResponse> response= restTemplate.exchange(
                url+"/callback",
                org.springframework.http.HttpMethod.POST,
                request,
                BankCallBackResponse.class
        );
        linkedBankService.callbackMockBank(response.getBody());
    }
}
