package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.Session;
import com.rentamovil.iam.domain.port.out.SessionRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.repository.SessionJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionRepositoryAdapter implements SessionRepositoryPort {

    private final SessionJpaRepository sessionJpaRepository;

    @Override
    public Session save(Session session) {
        return PersistenceMapper.toDomain(sessionJpaRepository.save(PersistenceMapper.toEntity(session)));
    }

    @Override
    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return sessionJpaRepository.findByRefreshToken(refreshTokenHash).map(PersistenceMapper::toDomain);
    }

    // Los UPDATE (@Modifying) necesitan una transacción
    @Override
    @Transactional
    public boolean revokeIfActive(Integer sessionId) {
        return sessionJpaRepository.revokeIfActive(sessionId) == 1;
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Integer userId) {
        sessionJpaRepository.revokeAllByUserId(userId);
    }
}
