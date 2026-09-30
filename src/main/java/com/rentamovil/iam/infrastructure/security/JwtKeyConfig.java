package com.rentamovil.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * Loads the RS256 signing key pair once at startup. The service refuses to start if the keys are
 * missing, malformed, too short, or not a matching pair — better than failing on the first login.
 */
@Configuration
public class JwtKeyConfig {

    private static final int MIN_KEY_SIZE_BITS = 2048;

    @Bean
    RSAKey jwtSigningKey(
            @Value("${app.iam.jwt.private-key:}") String privateKeyText,
            @Value("${app.iam.jwt.public-key:}") String publicKeyText) throws JOSEException {

        RSAPrivateKey privateKey = RsaKeyParser.parsePrivateKey(privateKeyText);
        RSAPublicKey publicKey = RsaKeyParser.parsePublicKey(publicKeyText);

        if (publicKey.getModulus().bitLength() < MIN_KEY_SIZE_BITS) {
            throw new IllegalStateException("JWT RSA key must be at least " + MIN_KEY_SIZE_BITS + " bits");
        }
        if (!isMatchingPair(privateKey, publicKey)) {
            throw new IllegalStateException(
                    "APP_IAM_JWT_PRIVATE_KEY and APP_IAM_JWT_PUBLIC_KEY are not a matching key pair");
        }

        RSAKey key = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        // kid = RFC 7638 thumbprint: derived from the public key itself, so it changes only when
        // the key changes (lets verifiers pick the right key during a future key rotation).
        return new RSAKey.Builder(key).keyID(key.computeThumbprint().toString()).build();
    }

    static boolean isMatchingPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
        try {
            byte[] probe = "rentamovil-key-pair-check".getBytes(StandardCharsets.UTF_8);
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(probe);
            byte[] signature = signer.sign();

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(probe);
            return verifier.verify(signature);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
