package com.rentamovil.iam.infrastructure.security;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

// Carga el par de claves RS256 del .env al arrancar.
// La privada firma los tokens; la pública se copia a mano al .env de cada servicio consumidor
// (APP_<SERVICIO>_JWT_PUBLIC_KEY) para que verifiquen la firma sin depender de un endpoint propio.
@Configuration
public class JwtKeyConfig {

    @Bean
    public RSAKey jwtSigningKey(@Value("${app.iam.jwt.private-key}") String privateKeyText,
                                @Value("${app.iam.jwt.public-key}") String publicKeyText) throws Exception {

        if (privateKeyText.isBlank() || publicKeyText.isBlank()) {
            throw new IllegalStateException("Faltan APP_IAM_JWT_PRIVATE_KEY / APP_IAM_JWT_PUBLIC_KEY en el .env");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decode(privateKeyText)));
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decode(publicKeyText)));

        //Si las dos claves no son pareja, los tokens firmados no se podrían verificar
        if (!publicKey.getModulus().equals(privateKey.getModulus())) {
            throw new IllegalStateException("La clave pública y la privada del .env no son pareja");
        }

        RSAKey key = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        //El kid es la "huella" de la clave: sirve para cambiar de clave en el futuro sin romper nada
        return new RSAKey.Builder(key).keyID(key.computeThumbprint().toString()).build();
    }

    // Acepta el PEM completo o solo el texto base64 en una línea (así cabe en el .env)
    private byte[] decode(String keyText) {
        String base64 = keyText.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
