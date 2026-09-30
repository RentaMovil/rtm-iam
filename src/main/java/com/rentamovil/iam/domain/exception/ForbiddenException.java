package com.rentamovil.iam.domain.exception;

// Quien llama no tiene el permiso necesario (ej. un ADMIN intentando cambiar un rol). Responde 403.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("No tienes permisos para realizar esta acción");
    }
}
