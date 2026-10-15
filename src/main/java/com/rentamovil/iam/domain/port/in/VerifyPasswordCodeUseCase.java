package com.rentamovil.iam.domain.port.in;

// Comprobar el código de recuperación sin gastarlo (HU-IAM-003, paso intermedio)
public interface VerifyPasswordCodeUseCase {

    void verifyCode(String email, String code);
}
