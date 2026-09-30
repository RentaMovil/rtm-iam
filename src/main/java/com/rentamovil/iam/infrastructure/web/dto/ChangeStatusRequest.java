package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de PATCH /users/{id}/status. BLOCKED no se acepta: a ese estado solo se llega solo.
@Getter
@Setter
public class ChangeStatusRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "debe ser ACTIVE o INACTIVE")
    private String status;
}
