package com.rentamovil.iam.application.service;

import java.time.LocalDateTime;

import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.port.out.PermissionRepositoryPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;

import lombok.RequiredArgsConstructor;

// Crea el par de tokens y la sesión. Lo usan el login y el refresh, para no repetir el código.
@RequiredArgsConstructor
public class AuthTokenService {

    private final PermissionRepositoryPort permissionRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final long refreshTokenExpirySeconds;

    public AuthTokens issue(User user, String ipAddress, String userAgent) {

        //Los permisos del rol van dentro del JWT
        user.getRole().setPermissions(permissionRepositoryPort.findNamesByRoleId(user.getRole().getId()));

        String accessToken = tokenProviderPort.generateAccessToken(user);
        String refreshToken = tokenProviderPort.generateRefreshToken();

        //Se guarda el hash del refresh token, no el token
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder()
                .userId(user.getId())
                .refreshTokenHash(tokenProviderPort.hashRefreshToken(refreshToken))
                .createdAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpirySeconds))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();
        sessionRepositoryPort.save(session);

        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProviderPort.getAccessTokenExpiresIn())
                .user(user)
                .build();
    }
}
