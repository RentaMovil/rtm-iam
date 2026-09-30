package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /register (RegisterRequest en iam.yaml)
@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "solo letras, números, punto y guion bajo")
    private String username;

    // Mínimo 8 caracteres, al menos una mayúscula y un número
    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "debe tener al menos una mayúscula y un número")
    private String password;
}
