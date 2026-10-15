package com.rentamovil.iam.domain.port.in;

import com.rentamovil.iam.domain.entity.Person;
import com.rentamovil.iam.domain.entity.User;

// Contrato para registrar un nuevo cliente (HU-IAM-001)
public interface RegisterUserUseCase {

    User register(Person person, String username, String rawPassword);
}
