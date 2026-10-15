package com.rentamovil.iam.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rentamovil.iam.infrastructure.persistence.entity.VerificationCodeEntity;

public interface VerificationCodeJpaRepository extends JpaRepository<VerificationCodeEntity, Integer> {

    Optional<VerificationCodeEntity> findFirstByUserIdAndTypeAndUsedFalseOrderByCreatedAtDescIdDesc(Integer userId, String type);

    @Modifying
    @Query("UPDATE VerificationCodeEntity v SET v.used = true WHERE v.userId = :userId AND v.type = :type AND v.used = false")
    int invalidateUnused(@Param("userId") Integer userId, @Param("type") String type);
}
