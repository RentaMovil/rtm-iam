package com.rentamovil.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.rentamovil.iam.domain.entity.User;
import com.rentamovil.iam.domain.exception.InvalidTokenException;
import com.rentamovil.iam.domain.port.out.TokenProviderPort;

// Crea los tokens. Claims definidos en 07-api/authentication.md: sub, email, role, permissions.
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final RSAKey jwtSigningKey;
    private final long accessTokenExpirySeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenProviderAdapter(RSAKey jwtSigningKey,
                                   @Value("${app.iam.jwt.expiry}") long accessTokenExpirySeconds) {
        this.jwtSigningKey = jwtSigningKey;
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getPerson().getEmail())
                // Un solo rol, nunca una lista
                .claim("role", user.getRole().getName())
                .claim("permissions", user.getRole().getPermissions())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(accessTokenExpirySeconds)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        //El kid en la cabecera le dice a quien verifica qué clave de /jwks usar
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(jwtSigningKey.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        try {
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(jwtSigningKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }

    @Override
    public Integer getUserIdFromAccessToken(String accessToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);

            //Solo se aceptan tokens RS256 firmados con nuestra clave
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new RSASSAVerifier(jwtSigningKey.toRSAPublicKey()))) {
                throw new InvalidTokenException("Token inválido");
            }

            Date expiration = jwt.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new InvalidTokenException("Token vencido");
            }

            return Integer.valueOf(jwt.getJWTClaimsSet().getSubject());
        } catch (ParseException | JOSEException | NumberFormatException e) {
            throw new InvalidTokenException("Token inválido");
        }
    }

    @Override
    public long getAccessTokenExpiresIn() {
        return accessTokenExpirySeconds;
    }

    // 32 bytes aleatorios: imposible de adivinar
    @Override
    public String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 en hexadecimal (64 caracteres). Alcanza porque el token ya es aleatorio;
    // bcrypt no hace falta aquí y además no permitiría buscar la sesión por el hash.
    @Override
    public String hashRefreshToken(String refreshToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
