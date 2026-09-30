package com.rentamovil.iam.domain.exception;

import lombok.Getter;

// Se lanza cuando se intenta crear algo que ya existe (ej. email o username duplicado)
@Getter
public class ResourceAlreadyExistsException extends RuntimeException {

    // Código de error del contrato (ej. EMAIL_ALREADY_EXISTS)
    private final String code;

    public ResourceAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
