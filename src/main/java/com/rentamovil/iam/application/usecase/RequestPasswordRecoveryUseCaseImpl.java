package com.rentamovil.iam.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.port.in.RequestPasswordRecoveryUseCase;
import com.rentamovil.iam.domain.port.out.EmailSenderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestPasswordRecoveryUseCaseImpl implements RequestPasswordRecoveryUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    private final EmailSenderPort emailSenderPort;
    private final long codeExpirySeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    // No devuelve nada ni lanza error si el email no existe: la respuesta es siempre la misma,
    // así nadie puede usar esta pantalla para averiguar qué emails están registrados.
    @Override
    public void requestRecovery(String email) {
        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase()).orElse(null);

        //Solo cuentas activas: una desactivada o bloqueada la maneja un SUPER_ADMIN
        if (user == null || !user.isActive()) {
            return;
        }

        //Si pidió varios códigos, solo sirve el último
        verificationCodeRepositoryPort.invalidateUnused(user.getId(), CodeType.PASSWORD_RESET);

        LocalDateTime now = LocalDateTime.now();
        VerificationCode verificationCode = VerificationCode.builder()
                .userId(user.getId())
                .code(generateSixDigitCode())
                .type(CodeType.PASSWORD_RESET)
                .createdAt(now)
                .expiresAt(now.plusSeconds(codeExpirySeconds))
                .used(false)
                .build();
        verificationCodeRepositoryPort.save(verificationCode);

        emailSenderPort.sendPasswordResetCode(user.getPerson().getEmail(), user.getPerson().getFirstName(),
                verificationCode.getCode());
    }

    // 000000 a 999999, con SecureRandom (Random normal se puede predecir)
    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
