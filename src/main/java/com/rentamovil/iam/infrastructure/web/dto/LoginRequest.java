package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /login (LoginRequest en iam.yaml)
@Getter
@Setter
public class LoginRequest {

    // Email o username
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
