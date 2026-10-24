package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rentamovil.iam.infrastructure.persistence.entity.PushDeviceTokenEntity;

public interface PushDeviceTokenJpaRepository extends JpaRepository<PushDeviceTokenEntity, Integer> {

    Optional<PushDeviceTokenEntity> findByToken(String token);

    List<PushDeviceTokenEntity> findByUserId(Integer userId);
}
