package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de PATCH /users/{id}/role
@Getter
@Setter
public class ChangeRoleRequest {

    @NotBlank
    @Pattern(regexp = "CLIENT|ADMIN|SUPER_ADMIN", message = "debe ser CLIENT, ADMIN o SUPER_ADMIN")
    private String role;
}
