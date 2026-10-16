package com.rentamovil.iam.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;

// Respuesta de POST /register: la cuenta queda PENDING_VERIFICATION, sin tokens todavía
// (HU-IAM-001). El frontend usa "email" para mandar al usuario a la pantalla de verificación.
@Getter
@Builder
public class RegisterResponse {

    private String email;
    private String message;
}
