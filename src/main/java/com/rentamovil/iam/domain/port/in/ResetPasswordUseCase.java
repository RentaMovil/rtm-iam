package com.rentamovil.iam.domain.port.in;

// Cambiar la contraseña con el código recibido (HU-IAM-003, paso 2)
public interface ResetPasswordUseCase {

    void resetPassword(String email, String code, String newPassword);
}
