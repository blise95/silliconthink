package com.silliconthink.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String tokenType;
    private UserInfoVO user;

    public static TokenResponse of(String accessToken, UserInfoVO user) {
        return new TokenResponse(accessToken, "Bearer", user);
    }
}
