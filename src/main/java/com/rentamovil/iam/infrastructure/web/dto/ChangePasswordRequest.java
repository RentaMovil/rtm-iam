package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de PATCH /users/me/password
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    // Misma regla que en el registro
    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "debe tener al menos una mayúscula y un número")
    private String newPassword;
}
