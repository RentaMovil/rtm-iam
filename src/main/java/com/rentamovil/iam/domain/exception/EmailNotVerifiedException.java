package com.rentamovil.iam.domain.exception;

// La cuenta está PENDING_VERIFICATION: todavía no confirmó el código que se le mandó al correo
// al registrarse (HU-IAM-001). No puede hacer login hasta pasar a ACTIVE.
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("Verifica tu correo antes de iniciar sesión");
    }
}
