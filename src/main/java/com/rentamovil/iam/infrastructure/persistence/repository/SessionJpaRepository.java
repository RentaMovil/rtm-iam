package com.rentamovil.iam.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.iam.infrastructure.persistence.entity.SessionEntity;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, Integer> {
}
