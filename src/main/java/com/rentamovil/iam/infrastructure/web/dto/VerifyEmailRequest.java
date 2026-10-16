package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /auth/email/verify
@Getter
@Setter
public class VerifyEmailRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "el código tiene 6 dígitos")
    private String code;
}
