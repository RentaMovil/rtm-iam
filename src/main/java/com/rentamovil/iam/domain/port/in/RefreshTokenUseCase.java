package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.AuthTokens;

// Contrato para renovar los tokens con el refresh token (rotación: el anterior deja de servir)
public interface RefreshTokenUseCase {

    AuthTokens refresh(String refreshToken, String ipAddress, String userAgent);
}
