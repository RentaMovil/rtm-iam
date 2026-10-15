package com.rentamovil.iam.domain.entity;

import java.time.LocalDateTime;

import com.rentamovil.iam.domain.enums.CodeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Código temporal de 6 dígitos (iam.verification_code). Sirve una sola vez y vence a los 15 min.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    private Integer id;
    private Integer userId;
    private String code;
    private CodeType type;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
