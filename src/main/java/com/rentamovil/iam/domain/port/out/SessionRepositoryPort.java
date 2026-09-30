package com.rentamovil.iam.domain.port.out;

import java.util.Optional;

import com.rentamovil.iam.domain.entity.Session;

public interface SessionRepositoryPort {

    Session save(Session session);

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    // Marca la sesión como revocada solo si todavía estaba activa.
    // Devuelve false si ya estaba revocada: así dos refresh al mismo tiempo no ganan los dos.
    boolean revokeIfActive(Integer sessionId);

    void revokeAllByUserId(Integer userId);
}
