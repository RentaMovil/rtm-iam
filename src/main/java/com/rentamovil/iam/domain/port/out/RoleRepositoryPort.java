package com.rentamovil.iam.domain.port.out;

import java.util.Optional;

import com.rentamovil.iam.domain.entity.Role;

// Los roles solo se leen: los 3 los siembra rtm-iam-db
public interface RoleRepositoryPort {

    Optional<Role> findByName(String name);
}
