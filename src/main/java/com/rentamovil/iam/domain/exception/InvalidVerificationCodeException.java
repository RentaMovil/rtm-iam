package com.rentamovil.iam.domain.exception;

import lombok.Getter;

// Código de verificación incorrecto, ya usado o vencido. Responde 400.
@Getter
public class InvalidVerificationCodeException extends RuntimeException {

    // INVALID_CODE o CODE_EXPIRED
    private final String code;

    public InvalidVerificationCodeException(String code, String message) {
        super(message);
        this.code = code;
    }
}
