package com.rentamovil.iam.domain.port.out;

import java.util.Optional;

import com.rentamovil.iam.domain.entity.VerificationCode;
import com.rentamovil.iam.domain.enums.CodeType;

public interface VerificationCodeRepositoryPort {

    VerificationCode save(VerificationCode verificationCode);

    // El último código sin usar de ese tipo (si pidió varios, solo vale el más nuevo)
    Optional<VerificationCode> findLatestUnused(Integer userId, CodeType type);

    // Marca como usados los códigos anteriores sin usar, para que solo sirva el nuevo
    void invalidateUnused(Integer userId, CodeType type);
}
