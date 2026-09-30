package com.rentamovil.iam.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.iam.infrastructure.persistence.entity.PersonEntity;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, Integer> {

    boolean existsByEmail(String email);
}
