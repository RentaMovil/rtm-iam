package com.rentamovil.iam.application.usecase;

import com.rentamovil.iam.domain.port.in.LogoutUseCase;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;

import lombok.RequiredArgsConstructor;

// El access token no se puede anular (dura 30 min y no hay lista negra, ver authentication.md).
// Cerrar sesión = revocar el refresh token para que no se puedan pedir tokens nuevos.
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final TokenProviderPort tokenProviderPort;

    @Override
    public void logout(Integer userId, String refreshToken, boolean allDevices) {

        //Cerrar sesión en todos los dispositivos
        if (allDevices) {
            sessionRepositoryPort.revokeAllByUserId(userId);
            return;
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        //Solo se revoca si la sesión es del mismo usuario del access token
        sessionRepositoryPort.findByRefreshTokenHash(tokenProviderPort.hashRefreshToken(refreshToken))
                .filter(session -> session.getUserId().equals(userId))
                .ifPresent(session -> sessionRepositoryPort.revokeIfActive(session.getId()));
    }
}
