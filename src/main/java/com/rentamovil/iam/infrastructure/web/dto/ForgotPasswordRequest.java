package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /password/forgot
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    private String email;
}
