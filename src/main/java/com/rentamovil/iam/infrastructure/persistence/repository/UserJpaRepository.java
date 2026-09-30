package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.iam.infrastructure.persistence.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    // SQL Server no distingue mayúsculas por defecto: "MGarcia" y "mgarcia" chocan
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    // Busca por el email de la persona asociada (user.person.email)
    Optional<UserEntity> findByPersonEmail(String email);

    // Para la pantalla de gestión de usuarios: busca en nombre, apellido, email y username
    @Query("""
            SELECT u FROM UserEntity u
            WHERE :search = ''
               OR LOWER(u.person.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.person.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.person.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY u.id
            """)
    List<UserEntity> search(@Param("search") String search);
}
