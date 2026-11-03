package es.codeurjc.students.trainfyre.security.infrastructure.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record JwtKeys(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

    private static final int MINIMUM_KEY_BITS = 2048;

    public JwtKeys {
        if (publicKey.getModulus().bitLength() < MINIMUM_KEY_BITS) {
            throw new IllegalStateException("JWT RSA key must be at least " + MINIMUM_KEY_BITS + " bits");
        }
        if (!publicKey.getModulus().equals(privateKey.getModulus())) {
            throw new IllegalStateException("JWT public key does not match the private key");
        }
    }

    @Override
    public String toString() {
        return "JwtKeys[****]";
    }
}
