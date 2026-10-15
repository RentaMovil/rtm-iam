package com.rentamovil.iam.domain.port.out;

import com.rentamovil.iam.domain.entity.AuthenticatedUser;
import com.rentamovil.iam.domain.entity.User;

// Contrato para crear los tokens. La implementación real (JWT RS256) vive en infrastructure/security.
public interface TokenProviderPort {

    // JWT firmado con sub, email, role y permissions
    String generateAccessToken(User user);

    // Verifica firma y vencimiento del access token y devuelve quién es (sub, role, permissions).
    // Lanza InvalidTokenException si no es válido.
    AuthenticatedUser validateAccessToken(String accessToken);

    // Segundos de vida del access token (1800 = 30 min)
    long getAccessTokenExpiresIn();

    // Token opaco (aleatorio) para POST /refresh
    String generateRefreshToken();

    // Hash del refresh token, que es lo que se guarda en iam.session
    String hashRefreshToken(String refreshToken);
}
