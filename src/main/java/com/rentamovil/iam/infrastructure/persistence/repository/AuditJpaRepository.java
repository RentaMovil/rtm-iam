package com.rentamovil.iam.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.iam.infrastructure.persistence.entity.AuditEntity;

public interface AuditJpaRepository extends JpaRepository<AuditEntity, Integer> {
}
