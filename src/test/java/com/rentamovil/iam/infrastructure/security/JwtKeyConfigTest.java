package com.rentamovil.iam.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

class JwtKeyConfigTest {

    private final JwtKeyConfig config = new JwtKeyConfig();

    @Test
    void loadsKeyPairFromOneLineBase64() throws Exception {
        KeyPair pair = rsa(2048);

        RSAKey key = config.jwtSigningKey(base64(pair.getPrivate().getEncoded()), base64(pair.getPublic().getEncoded()));

        assertThat(key.isPrivate()).isTrue();
        assertThat(key.getAlgorithm()).isEqualTo(JWSAlgorithm.RS256);
        assertThat(key.getKeyUse()).isEqualTo(KeyUse.SIGNATURE);
        assertThat(key.getKeyID()).isNotBlank();
    }

    @Test
    void loadsKeyPairFromPem() throws Exception {
        KeyPair pair = rsa(2048);
        String privatePem = pem("PRIVATE KEY", pair.getPrivate().getEncoded());
        String publicPem = pem("PUBLIC KEY", pair.getPublic().getEncoded());

        RSAKey key = config.jwtSigningKey(privatePem, publicPem);

        assertThat(key.toRSAPublicKey()).isEqualTo(pair.getPublic());
    }

    @Test
    void keyIdIsStableForTheSameKey() throws Exception {
        KeyPair pair = rsa(2048);
        String priv = base64(pair.getPrivate().getEncoded());
        String pub = base64(pair.getPublic().getEncoded());

        assertThat(config.jwtSigningKey(priv, pub).getKeyID()).isEqualTo(config.jwtSigningKey(priv, pub).getKeyID());
    }

    @Test
    void refusesMissingKeys() {
        assertThatThrownBy(() -> config.jwtSigningKey("", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_IAM_JWT_PRIVATE_KEY is not set");
    }

    @Test
    void refusesGarbage() {
        assertThatThrownBy(() -> config.jwtSigningKey("not a key", "not a key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_IAM_JWT_PRIVATE_KEY");
    }

    @Test
    void refusesKeysThatAreNotAPair() throws Exception {
        KeyPair one = rsa(2048);
        KeyPair other = rsa(2048);

        assertThatThrownBy(() -> config.jwtSigningKey(
                base64(one.getPrivate().getEncoded()), base64(other.getPublic().getEncoded())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a matching key pair");
    }

    @Test
    void refusesKeysShorterThan2048Bits() throws Exception {
        KeyPair weak = rsa(1024);

        assertThatThrownBy(() -> config.jwtSigningKey(
                base64(weak.getPrivate().getEncoded()), base64(weak.getPublic().getEncoded())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2048 bits");
    }

    static KeyPair rsa(int bits) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(bits);
        return generator.generateKeyPair();
    }

    static String base64(byte[] der) {
        return Base64.getEncoder().encodeToString(der);
    }

    private static String pem(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }
}
