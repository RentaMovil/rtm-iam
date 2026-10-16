package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.AuthTokens;

// HU-IAM-001: confirma el código enviado al registrarse. Si es válido, activa la cuenta y la
// deja con sesión iniciada (mismo criterio que el registro: no hacerla loguearse dos veces).
public interface VerifyEmailUseCase {

    AuthTokens verifyEmail(String email, String code, String ipAddress, String userAgent);
}
