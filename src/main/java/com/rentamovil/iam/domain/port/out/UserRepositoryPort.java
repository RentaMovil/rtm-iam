package com.rentamovil.iam.domain.port.out;

import java.util.Optional;

import com.rentamovil.iam.domain.entity.User;

// Lo que el dominio necesita para guardar y consultar usuarios.
// La implementación real (JPA / SQL Server) vive en infrastructure/persistence.
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Integer id);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
