package com.rentamovil.iam.application.usecase;

import java.time.LocalDateTime;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.common.exception.InvalidTokenException;
import com.rentamovil.iam.domain.port.in.RefreshTokenUseCase;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

// Sin @Transactional por la misma razón que el login: si detecta un token reutilizado revoca
// todas las sesiones y después lanza la excepción; una transacción desharía esa revocación.
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final AuthTokenService authTokenService;

    @Override
    public AuthTokens refresh(String refreshToken, String ipAddress, String userAgent) {

        //En la base está el hash, así que se busca por el hash del token recibido
        Session session = sessionRepositoryPort.findByRefreshTokenHash(tokenProviderPort.hashRefreshToken(refreshToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));

        //Un refresh token solo sirve una vez. Si llega uno ya usado, alguien lo pudo haber robado:
        //se cierran todas las sesiones del usuario (security-policy.md)
        if (session.isRevoked() || !sessionRepositoryPort.revokeIfActive(session.getId())) {
            sessionRepositoryPort.revokeAllByUserId(session.getUserId());
            throw new InvalidTokenException("Refresh token ya usado. Se cerraron todas las sesiones");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token vencido. Inicia sesión de nuevo");
        }

        //Si al usuario lo bloquearon o desactivaron después del login, ya no puede renovar
        User user = userRepositoryPort.findById(session.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidTokenException("La cuenta ya no está activa"));

        return authTokenService.issue(user, ipAddress, userAgent);
    }
}
