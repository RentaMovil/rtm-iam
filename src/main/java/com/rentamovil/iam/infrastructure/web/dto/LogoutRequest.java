package com.rentamovil.iam.infrastructure.web.dto;

import lombok.Getter;
import lombok.Setter;

// Cuerpo de POST /logout (opcional)
@Getter
@Setter
public class LogoutRequest {

    // Refresh token de la sesión a cerrar
    private String refreshToken;

    // true = cerrar sesión en todos los dispositivos
    private boolean allDevices;
}
