package com.mockbank.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallBackRequest {
    private String requestId;
    private String type;
    private String token;
    private String walletNo;
}
