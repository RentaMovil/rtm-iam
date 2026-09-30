package com.rentamovil.iam.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

class JwksControllerTest {

    @Test
    @SuppressWarnings("unchecked")
    void exposesOnlyThePublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        RSAKey signingKey = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("test-kid")
                .build();

        var response = new JwksController(signingKey).jwks();

        List<Map<String, Object>> keys = (List<Map<String, Object>>) response.getBody().get("keys");
        assertThat(keys).hasSize(1);
        Map<String, Object> jwk = keys.get(0);
        assertThat(jwk).containsEntry("kty", "RSA")
                .containsEntry("alg", "RS256")
                .containsEntry("use", "sig")
                .containsEntry("kid", "test-kid")
                .containsKeys("n", "e");
        // RSA private components (RFC 7518 §6.3.2) must never leave the service
        assertThat(jwk).doesNotContainKeys("d", "p", "q", "dp", "dq", "qi", "oth");
        assertThat(response.getHeaders().getCacheControl()).contains("max-age=300");
    }
}
