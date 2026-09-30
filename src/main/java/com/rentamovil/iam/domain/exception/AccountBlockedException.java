package com.rentamovil.iam.domain.exception;

// La cuenta está BLOCKED por intentos fallidos. Solo un SUPER_ADMIN la puede desbloquear (HU-IAM-005).
public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException() {
        super("La cuenta está bloqueada. Contacta a un administrador");
    }
}
