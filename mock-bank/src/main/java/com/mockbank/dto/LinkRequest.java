package com.mockbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkRequest {
    @NotBlank(message = "partnerId is required")
    private String partnerId;
    private String bankNo;
}
