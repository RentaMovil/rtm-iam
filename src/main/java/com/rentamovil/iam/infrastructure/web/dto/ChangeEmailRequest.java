package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de PATCH /users/me/email
@Getter
@Setter
public class ChangeEmailRequest {

    @NotBlank
    @Email
    @Size(max = 150)
    private String newEmail;

    @NotBlank
    private String currentPassword;
}
