package com.rentamovil.iam.domain.exception;

// Usuario inexistente o contraseña incorrecta. Mismo mensaje para los dos casos, a propósito:
// así no se puede averiguar qué emails están registrados.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email/usuario o contraseña incorrectos");
    }
}
