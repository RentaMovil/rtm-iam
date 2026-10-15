package com.rentamovil.iam.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// Lo que se le entrega al usuario cuando inicia sesión
@Getter
@AllArgsConstructor
@Builder
public class AuthTokens {

    private String accessToken;
    private String refreshToken;

    // Segundos hasta que vence el access token
    private long expiresIn;

    private User user;
}
