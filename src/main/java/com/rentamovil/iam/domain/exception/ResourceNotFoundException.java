package com.rentamovil.iam.domain.exception;

// Se lanza cuando algo que se busca no existe (ej. el rol CLIENT no está sembrado)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
