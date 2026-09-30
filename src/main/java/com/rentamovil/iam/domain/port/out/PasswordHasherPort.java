package com.rentamovil.iam.domain.port.out;

// Contrato para encriptar y verificar contraseñas.
// La implementación real (BCrypt) vive en infrastructure/security.
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
