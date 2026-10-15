package com.rentamovil.iam.domain.exception;

import lombok.Getter;

// Una regla del negocio no permite la operación (ej. desactivar una cuenta bloqueada). Responde 409.
@Getter
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
