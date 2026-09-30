package com.rentamovil.iam.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

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
}
