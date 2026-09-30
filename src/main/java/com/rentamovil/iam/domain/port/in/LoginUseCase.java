package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.AuthTokens;

// Contrato para iniciar sesión con email o username (HU-IAM-002)
public interface LoginUseCase {

    AuthTokens login(String identifier, String rawPassword, String ipAddress, String userAgent);
}
