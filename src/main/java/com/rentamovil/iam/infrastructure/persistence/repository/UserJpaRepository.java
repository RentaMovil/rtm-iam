package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.iam.infrastructure.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    // SQL Server no distingue mayúsculas por defecto: "MGarcia" y "mgarcia" chocan
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    // Busca por el email de la persona asociada (user.person.email)
    Optional<UserEntity> findByPersonEmail(String email);
}
