package com.rentamovil.iam.infrastructure.web.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;

// Publica la clave PÚBLICA para que el gateway y los demás servicios verifiquen nuestros tokens
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RSAKey jwtSigningKey;

    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() {
        //toPublicJWK() quita las partes privadas de la clave: nunca deben salir del servicio
        JWKSet publicKeys = new JWKSet(jwtSigningKey.toPublicJWK());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(publicKeys.toJSONObject());
    }
}
