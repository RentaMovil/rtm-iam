package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;

// Cambiar el rol de un usuario (HU-IAM-005 escenarios 1 y 2). Requiere roles:grant (solo SUPER_ADMIN).
public interface ChangeUserRoleUseCase {

    User changeRole(AuthenticatedUser caller, Integer userId, String roleName);
}
