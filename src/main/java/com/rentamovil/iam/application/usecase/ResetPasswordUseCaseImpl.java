package com.rentamovil.iam.application.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.exception.InvalidVerificationCodeException;
import com.rentamovil.iam.domain.port.in.ResetPasswordUseCase;
import com.rentamovil.iam.domain.port.out.AuditPort;
import com.rentamovil.iam.domain.port.out.PasswordHasherPort;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final SessionRepositoryPort sessionRepositoryPort;
    private final AuditPort auditPort;

    // Aquí sí va @Transactional: todos los errores se lanzan ANTES de guardar algo, y los 4 cambios
    // (contraseña, código usado, sesiones, auditoría) deben quedar juntos o no quedar.
    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {

        //Si el email no existe se responde igual que con un código malo, para no revelar nada
        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase())
                .orElseThrow(this::invalidCode);

        VerificationCode verificationCode = verificationCodeRepositoryPort
                .findLatestUnused(user.getId(), CodeType.PASSWORD_RESET)
                .orElseThrow(this::invalidCode);

        //Comparación en tiempo constante: no deja adivinar el código midiendo cuánto tarda la respuesta
        if (!MessageDigest.isEqual(verificationCode.getCode().getBytes(StandardCharsets.UTF_8),
                code.trim().getBytes(StandardCharsets.UTF_8))) {
            throw invalidCode();
        }

        if (verificationCode.isExpired()) {
            throw new InvalidVerificationCodeException("CODE_EXPIRED", "El código venció. Pide uno nuevo");
        }

        user.setPasswordHash(passwordHasherPort.hash(newPassword));
        userRepositoryPort.save(user);

        //El código ya no se puede volver a usar
        verificationCode.setUsed(true);
        verificationCodeRepositoryPort.save(verificationCode);

        //Al cambiar la contraseña se cierran todas las sesiones (security-policy.md)
        sessionRepositoryPort.revokeAllByUserId(user.getId());

        //PasswordChanged: solo auditoría (domain-events.md). Nunca se guarda la contraseña.
        auditPort.record(user.getId(), "PASSWORD_CHANGED", "user", user.getId(), Map.of("method", "PASSWORD_RESET_CODE"));
    }

    private InvalidVerificationCodeException invalidCode() {
        return new InvalidVerificationCodeException("INVALID_CODE", "El código no es válido");
    }
}
