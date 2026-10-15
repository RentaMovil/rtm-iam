package com.rentamovil.iam.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.port.in.VerifyPasswordCodeUseCase;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerifyPasswordCodeUseCaseImpl implements VerifyPasswordCodeUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;

    // Solo lectura: NO marca el código como usado, lo necesita /password/reset después.
    // Responde igual que reset (email inexistente = código malo) para no revelar qué cuentas existen.
    @Override
    public void verifyCode(String email, String code) {
        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase())
                .orElseThrow(this::invalidCode);

        VerificationCode verificationCode = verificationCodeRepositoryPort
                .findLatestUnused(user.getId(), CodeType.PASSWORD_RESET)
                .orElseThrow(this::invalidCode);

        //Comparación en tiempo constante: no deja adivinar el código midiendo cuánto tarda la respuesta
        if (!MessageDigest.isEqual(verificationCode.getCode().getBytes(StandardCharsets.UTF_8),
                code.trim().getBytes(StandardCharsets.UTF_8))) {
            throw invalidCode();
        }

        if (verificationCode.isExpired()) {
            throw new InvalidVerificationCodeException("CODE_EXPIRED", "El código venció. Pide uno nuevo");
        }
    }

    private InvalidVerificationCodeException invalidCode() {
        return new InvalidVerificationCodeException("INVALID_CODE", "El código no es válido");
    }
}
