package com.rentamovil.iam.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rentamovil.iam.domain.entity.AuthTokens;

import lombok.Builder;
import lombok.Getter;

// AuthResponse de iam.yaml
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    // null cuando el cliente es web: ese refresh token ya se mandó por cookie httpOnly
    // (ADR-005) y no debe quedar también en el body, donde un XSS sí lo podría leer.
    private String refreshToken;
    private long expiresIn;
    private UserResponse user;

    public static AuthResponse from(AuthTokens tokens, boolean omitRefreshToken) {
        return AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(omitRefreshToken ? null : tokens.getRefreshToken())
                .expiresIn(tokens.getExpiresIn())
                .user(UserResponse.from(tokens.getUser()))
                .build();
    }
}
