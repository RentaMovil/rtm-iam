package com.rentamovil.iam.domain.port.in;

// Pedir el código de recuperación por email (HU-IAM-003, paso 1)
public interface RequestPasswordRecoveryUseCase {

    void requestRecovery(String email);
}
