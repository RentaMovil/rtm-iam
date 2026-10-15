package com.rentamovil.iam.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.rentamovil.iam.domain.port.out.PasswordHasherPort;

// BCrypt con costo mínimo 12 (security-rules.md). Nunca MD5 ni SHA para contraseñas.
@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(@Value("${app.iam.bcrypt-rounds}") int rounds) {
        if (rounds < 12) {
            throw new IllegalStateException("APP_IAM_BCRYPT_ROUNDS debe ser 12 o más");
        }
        this.encoder = new BCryptPasswordEncoder(rounds);
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
