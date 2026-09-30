package com.rentamovil.iam.infrastructure.security;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Turns the key text from APP_IAM_JWT_PRIVATE_KEY / APP_IAM_JWT_PUBLIC_KEY into Java keys.
 *
 * <p>Accepts either a full PEM block ("-----BEGIN PRIVATE KEY-----" ...) or just its Base64 body on
 * one line, which is what fits in a .env file. Private key: PKCS#8. Public key: X.509
 * SubjectPublicKeyInfo. Both are what {@code openssl genpkey} / {@code openssl pkey -pubout} produce.
 */
final class RsaKeyParser {

    private RsaKeyParser() {
    }

    static RSAPrivateKey parsePrivateKey(String text) {
        try {
            var spec = new PKCS8EncodedKeySpec(decode(text, "APP_IAM_JWT_PRIVATE_KEY"));
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (GeneralSecurityException | ClassCastException e) {
            throw new IllegalStateException(
                    "APP_IAM_JWT_PRIVATE_KEY is not a valid RSA private key in PKCS#8 format", e);
        }
    }

    static RSAPublicKey parsePublicKey(String text) {
        try {
            var spec = new X509EncodedKeySpec(decode(text, "APP_IAM_JWT_PUBLIC_KEY"));
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (GeneralSecurityException | ClassCastException e) {
            throw new IllegalStateException(
                    "APP_IAM_JWT_PUBLIC_KEY is not a valid RSA public key in X.509 format", e);
        }
    }

    private static byte[] decode(String text, String variable) {
        if (text == null || text.isBlank()) {
            throw new IllegalStateException(variable + " is not set (see .env.example)");
        }
        String base64 = text
                .replaceAll("-----(BEGIN|END) [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(variable + " is not valid Base64/PEM", e);
        }
    }
}
