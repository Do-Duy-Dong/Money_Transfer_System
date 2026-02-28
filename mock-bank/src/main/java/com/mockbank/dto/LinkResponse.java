package com.mockbank.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkResponse {
    private String status;
    private String partnerId;
    private String token;
    private String secretKey;
    private String createdAt;
}
