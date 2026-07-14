package com.silliconthink.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthExchangeRequest {

    @NotBlank(message = "code is required")
    private String code;
}
