package com.rentamovil.iam.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.enums.UserStatus;
import com.rentamovil.iam.domain.port.in.ResendEmailVerificationUseCase;
import com.rentamovil.iam.domain.port.out.EmailSenderPort;
import com.rentamovil.iam.domain.port.out.UserRepositoryPort;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResendEmailVerificationUseCaseImpl implements ResendEmailVerificationUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final VerificationCodeRepositoryPort verificationCodeRepositoryPort;
    private final EmailSenderPort emailSenderPort;
    private final long codeExpirySeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    // No revela si el email existe ni en qué estado está la cuenta: si no corresponde a una
    // cuenta PENDING_VERIFICATION, simplemente no hace nada.
    @Override
    public void resend(String email) {
        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (user == null || user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            return;
        }

        //Si pidió varios códigos, solo sirve el último
        verificationCodeRepositoryPort.invalidateUnused(user.getId(), CodeType.EMAIL_VERIFICATION);

        LocalDateTime now = LocalDateTime.now();
        VerificationCode verificationCode = VerificationCode.builder()
                .userId(user.getId())
                .code(generateSixDigitCode())
                .type(CodeType.EMAIL_VERIFICATION)
                .createdAt(now)
                .expiresAt(now.plusSeconds(codeExpirySeconds))
                .used(false)
                .build();
        verificationCodeRepositoryPort.save(verificationCode);

        emailSenderPort.sendEmailVerificationCode(user.getPerson().getEmail(), user.getPerson().getFirstName(),
                verificationCode.getCode());
    }

    // 000000 a 999999, con SecureRandom (Random normal se puede predecir)
    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
