package com.rentamovil.iam.infrastructure.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentamovil.iam.domain.port.in.GetPushTokensUseCase;
import com.rentamovil.iam.infrastructure.web.dto.PushTokenResponse;

import lombok.RequiredArgsConstructor;

/**
 * Los tokens de Expo Push de un usuario, para que otro servicio (hoy: booking-reservation, al
 * crear una Notification) pueda mandarle un push.
 *
 * <p><b>Sin JWT a propósito</b>, igual que /internal/** del resto de servicios: lo llama otro
 * servicio, no una persona. Protegido por {@code InternalApiKeyFilter} (header X-Internal-Key).
 * El api-gateway no enruta /internal/**, así que no es alcanzable desde afuera.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final GetPushTokensUseCase getPushTokensUseCase;

    @GetMapping("/{id}/push-tokens")
    public List<PushTokenResponse> getPushTokens(@PathVariable Integer id) {
        return getPushTokensUseCase.getTokens(id).stream()
                .map(PushTokenResponse::from)
                .toList();
    }
}
