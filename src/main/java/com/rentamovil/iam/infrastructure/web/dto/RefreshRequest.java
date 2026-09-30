package com.rentamovil.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /refresh
@Getter
@Setter
public class RefreshRequest {

    @NotBlank
    private String refreshToken;
}
