package com.rentamovil.iam.domain.port.in;

// Contrato para cerrar sesión: revoca un refresh token, o todos los del usuario
public interface LogoutUseCase {

    void logout(Integer userId, String refreshToken, boolean allDevices);
}
