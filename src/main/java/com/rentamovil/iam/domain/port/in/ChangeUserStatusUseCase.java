package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.enums.UserStatus;

// Desbloquear o desactivar una cuenta (HU-IAM-005 escenarios 3 y 4). Requiere users:update-status.
public interface ChangeUserStatusUseCase {

    User changeStatus(AuthenticatedUser caller, Integer userId, UserStatus newStatus);
}
