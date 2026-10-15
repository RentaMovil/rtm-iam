package com.rentamovil.iam.infrastructure.web.dto;

import com.rentamovil.iam.domain.entity.AuthTokens;

import lombok.Builder;
import lombok.Getter;

// AuthResponse de iam.yaml
@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserResponse user;

    public static AuthResponse from(AuthTokens tokens) {
        return AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .expiresIn(tokens.getExpiresIn())
                .user(UserResponse.from(tokens.getUser()))
                .build();
    }
}
