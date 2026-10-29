package com.rentamovil.iam.infrastructure.web.dto;

import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /refresh. Opcional: web lo manda por cookie httpOnly (ADR-005), no aquí.
// AuthController valida que llegue por uno de los dos caminos.
@Getter
@Setter
public class RefreshRequest {

    private String refreshToken;
}
