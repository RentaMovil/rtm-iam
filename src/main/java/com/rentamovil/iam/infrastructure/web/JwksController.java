package com.rentamovil.iam.infrastructure.web;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * GET /jwks — the public half of the signing key as a JSON Web Key Set (RFC 7517), so the gateway
 * and the other services can verify iam's RS256 tokens locally. Public on purpose (no auth).
 */
@RestController
public class JwksController {

    private static final Duration CACHE_FOR = Duration.ofMinutes(5);

    private final Map<String, Object> publicJwkSet;

    public JwksController(RSAKey jwtSigningKey) {
        // toPublicJWK() drops every private component; computed once, the key never changes at runtime
        this.publicJwkSet = new JWKSet(jwtSigningKey.toPublicJWK()).toJSONObject();
    }

    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_FOR).cachePublic())
                .body(publicJwkSet);
    }
}
