package com.rentamovil.iam.infrastructure.web.dto;

import com.rentamovil.iam.domain.entity.PushDeviceToken;

// Lo que /internal/users/{id}/push-tokens le devuelve a los otros servicios.
public record PushTokenResponse(String token, String platform) {

    public static PushTokenResponse from(PushDeviceToken deviceToken) {
        return new PushTokenResponse(deviceToken.getToken(), deviceToken.getPlatform());
    }
}
