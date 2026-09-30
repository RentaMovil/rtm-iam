package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /password/reset
@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "el código tiene 6 dígitos")
    private String code;

    // Misma regla que en el registro
    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "debe tener al menos una mayúscula y un número")
    private String newPassword;
}
