package com.rentamovil.iam.domain.exception;

// Token inválido, vencido, revocado o ya usado. Siempre responde 401.
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
