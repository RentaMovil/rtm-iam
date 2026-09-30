package com.rentamovil.iam.domain.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una sesión abierta = un refresh token vigente (iam.session)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    private Integer id;
    private Integer userId;

    // Se guarda el hash del refresh token, no el token: si alguien lee la tabla no puede usarlos
    private String refreshTokenHash;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String ipAddress;
    private String userAgent;
    private boolean revoked;
}
