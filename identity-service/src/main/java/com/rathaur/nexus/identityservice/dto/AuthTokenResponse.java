package com.rathaur.nexus.identityservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Data
public class AuthTokenResponse {
    private String tokenType;   // "Bearer"
    private String accessToken;
    private String refreshToken;
    private long expiresInSeconds;
    private String username;
    private List<String> roles;

    public AuthTokenResponse() {}

    public AuthTokenResponse(String tokenType, String accessToken, String refreshToken,
                             long expiresInSeconds, String username, List<String> roles) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.username = username;
        this.roles = roles;
    }

}
