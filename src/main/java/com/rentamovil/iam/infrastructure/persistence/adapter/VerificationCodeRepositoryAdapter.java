package com.rentamovil.iam.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;
import com.rentamovil.iam.domain.port.out.VerificationCodeRepositoryPort;
import com.rentamovil.iam.infrastructure.persistence.repository.VerificationCodeJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VerificationCodeRepositoryAdapter implements VerificationCodeRepositoryPort {

    private final VerificationCodeJpaRepository verificationCodeJpaRepository;

    @Override
    public VerificationCode save(VerificationCode verificationCode) {
        return PersistenceMapper.toDomain(verificationCodeJpaRepository.save(PersistenceMapper.toEntity(verificationCode)));
    }

    @Override
    public Optional<VerificationCode> findLatestUnused(Integer userId, CodeType type) {
        return verificationCodeJpaRepository
                .findFirstByUserIdAndTypeAndUsedFalseOrderByCreatedAtDescIdDesc(userId, type.name())
                .map(PersistenceMapper::toDomain);
    }

    // Los UPDATE (@Modifying) necesitan una transacción
    @Override
    @Transactional
    public void invalidateUnused(Integer userId, CodeType type) {
        verificationCodeJpaRepository.invalidateUnused(userId, type.name());
    }
}
