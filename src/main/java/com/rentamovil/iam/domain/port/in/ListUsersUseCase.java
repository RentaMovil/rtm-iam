package com.rentamovil.iam.domain.port.in;

import java.util.List;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;

// Listar usuarios para la pantalla de gestión (HU-IAM-005). Requiere users:read.
public interface ListUsersUseCase {

    List<User> listUsers(AuthenticatedUser caller, String search);
}
