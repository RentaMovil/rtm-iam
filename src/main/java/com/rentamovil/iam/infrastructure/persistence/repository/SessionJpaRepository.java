package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.iam.infrastructure.persistence.entity.SessionEntity;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, Integer> {

    Optional<SessionEntity> findByRefreshToken(String refreshToken);

    // UPDATE con la condición "revoked = false": si dos peticiones llegan a la vez,
    // solo una cambia la fila (devuelve 1) y la otra devuelve 0
    @Modifying
    @Query("UPDATE SessionEntity s SET s.revoked = true WHERE s.id = :id AND s.revoked = false")
    int revokeIfActive(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE SessionEntity s SET s.revoked = true WHERE s.userId = :userId AND s.revoked = false")
    int revokeAllByUserId(@Param("userId") Integer userId);
}
