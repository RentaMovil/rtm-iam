package com.rentamovil.iam.domain.exception;

// La contraseña actual no coincide (ej. al cambiar el email, HU-IAM-004 escenario 3)
public class IncorrectPasswordException extends RuntimeException {

    public IncorrectPasswordException() {
        super("Contraseña incorrecta");
    }
}
