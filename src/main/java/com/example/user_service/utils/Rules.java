package com.example.user_service.utils;

import com.example.user_service.dto.ContextInput;
import com.example.user_service.dto.RequestChangeFund;
import com.example.user_service.dto.TransactionRule;
import com.example.user_service.exception.ResourceNotFound;
import com.example.user_service.model.Account;
import com.example.user_service.model.RuleEnum;
import com.example.user_service.model.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Rules {
    @Value("${signature.secret}")
    private String secretKey;
    private Map<RuleEnum, List<TransactionRule>> rules;

    private final RedisTemplate<String,Object> redisTemplate;

    public Rules(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.rules= new HashMap<>();
        this.rules.put(RuleEnum.TRANSFER, List.of(
                this::impodenticyKeyRule,
                this::balanceRule,
//                this::dailyLimitRule,
                this::signatureRule
        ));
        this.rules.put(RuleEnum.DEPOSIT, List.of(
                this::impodenticyKeyRule,
                this::signatureRule
        ));
        this.rules.put(RuleEnum.WITHDRAW, List.of(
                this::impodenticyKeyRule,
                this::balanceRule,
                this::signatureRule
        ));
        this.rules.put(RuleEnum.BALANCE, List.of(
                this::balanceRule
        ));
        this.rules.put(RuleEnum.IDEMPOTENCY, List.of(
                this::impodenticyKeyRule
        ));
    }
    private void impodenticyKeyRule(ContextInput request){
        if(redisTemplate.hasKey(
                request.getRuleEnum().name()
                +request.getRequestChangeFund().getRequestId())){
            throw new ResourceNotFound("Wait for previous request to expire");
        }
    }
    private void balanceRule(ContextInput request){
        Long available= request.getAccount().getBalance()-request.getAccount().getHeld();
        if(available < request.getRequestChangeFund().getAmount()){
            throw new ResourceNotFound("Not enough balance");
        }
    }
    private void dailyLimitRule(ContextInput request){

    }
    private void signatureRule(ContextInput request){
        String signature = HmacSignature.calculateHMAC(
                request.getRequestChangeFund().getRequestId(),
                request.getRequestChangeFund().getAccountNumber(),
                request.getRequestChangeFund().getLinkBankNo(),
                request.getRequestChangeFund().getAmount(),
                secretKey
        );
        if(!signature.equals(request.getRequestChangeFund().getSignature())){
            throw new ResourceNotFound("Invalid signature");
        }
    }
    public void validate(ContextInput request){
        List<TransactionRule> ruleList= rules.get(request.getRuleEnum());
        for(TransactionRule rule:ruleList){
            rule.validate(request);
        }
    }
}
