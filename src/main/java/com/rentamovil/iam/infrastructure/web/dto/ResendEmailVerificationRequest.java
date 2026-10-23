package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /auth/email/resend
@Getter
@Setter
public class ResendEmailVerificationRequest {

    @NotBlank
    @Email
    private String email;
}
