package com.rentamovil.iam.domain.port.out;

import com.rentamovil.iam.domain.entity.User;

// Lo que el dominio necesita para guardar y consultar usuarios.
// La implementación real (JPA / SQL Server) vive en infrastructure/persistence.
public interface UserRepositoryPort {

    User save(User user);

    boolean existsByUsername(String username);
}
