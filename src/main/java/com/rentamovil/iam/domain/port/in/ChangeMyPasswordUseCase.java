package com.rentamovil.iam.domain.port.in;

// Cambiar la contraseña estando logueado, confirmando con la actual. Cierra todas las sesiones.
public interface ChangeMyPasswordUseCase {

    void changePassword(Integer userId, String currentPassword, String newPassword);
}
