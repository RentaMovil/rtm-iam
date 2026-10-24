package com.rentamovil.iam.domain.port.in;

// El móvil llama esto al loguearse para registrar el token de Expo Push de este dispositivo.
public interface RegisterPushTokenUseCase {

    void registerToken(Integer userId, String token, String platform);
}
