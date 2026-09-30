package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.User;

// Cambiar el email confirmando con la contraseña actual (HU-IAM-004 escenarios 2-4)
public interface ChangeMyEmailUseCase {

    User changeEmail(Integer userId, String newEmail, String currentPassword);
}
