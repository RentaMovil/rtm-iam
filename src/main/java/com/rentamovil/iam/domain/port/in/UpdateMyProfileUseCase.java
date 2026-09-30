package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.User;

// Actualizar nombre, apellido y/o teléfono (HU-IAM-004 escenario 1). Lo que venga null no se cambia.
public interface UpdateMyProfileUseCase {

    User updateProfile(Integer userId, String firstName, String lastName, String phone);
}
