package com.rentamovil.iam.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.rentamovil.iam.application.service.AuthTokenService;
import com.rentamovil.iam.domain.entity.AuthTokens;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.port.in.VerifyEmailUseCase;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    private final AuthTokenService authTokenService;

    @Override
    public AuthTokens verifyEmail(String email, String code, String ipAddress, String userAgent) {
        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase())
                .orElseThrow(this::invalidCode);

        // Ya verificada (o bloqueada/inactiva): no tiene sentido re-verificar, se trata igual
        // que un código inválido en vez de revelar en qué estado está la cuenta.
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw invalidCode();
        }

        VerificationCode verificationCode = verificationCodeRepositoryPort
                .findLatestUnused(user.getId(), CodeType.EMAIL_VERIFICATION)
                .orElseThrow(this::invalidCode);

        //Comparación en tiempo constante: no deja adivinar el código midiendo cuánto tarda la respuesta
        if (!MessageDigest.isEqual(verificationCode.getCode().getBytes(StandardCharsets.UTF_8),
                code.trim().getBytes(StandardCharsets.UTF_8))) {
            throw invalidCode();
        }

        if (verificationCode.isExpired()) {
            throw new InvalidVerificationCodeException("CODE_EXPIRED", "El código venció. Pide uno nuevo");
        }

        verificationCode.setUsed(true);
        verificationCodeRepositoryPort.save(verificationCode);

        user.setStatus(UserStatus.ACTIVE);
        User activeUser = userRepositoryPort.save(user);

        return authTokenService.issue(activeUser, ipAddress, userAgent);
    }

    private InvalidVerificationCodeException invalidCode() {
        return new InvalidVerificationCodeException("INVALID_CODE", "El código no es válido");
    }
}
