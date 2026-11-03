package es.codeurjc.students.trainfyre.security.infrastructure.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtConfiguration {

    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public JwtKeys jwtKeys(SecurityProperties properties) {
        return new JwtKeys(
                readPublicKey(properties.jwt().publicKey()),
                readPrivateKey(properties.jwt().privateKey())
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtKeys keys, SecurityProperties properties) {
        RSAKey signingKey = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(properties.jwt().keyId())
                .build();

        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtKeys keys, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(keys.publicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        decoder.setJwtValidator(jwtValidator(properties));

        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> jwtValidator(SecurityProperties properties) {
        String audience = properties.jwt().audience();

        return new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(CLOCK_SKEW),
                new JwtIssuerValidator(properties.jwt().issuer()),
                new JwtClaimValidator<List<String>>(
                        JwtClaimNames.AUD,
                        audiences -> audiences != null && audiences.contains(audience)
                ),
                new JwtClaimValidator<String>(
                        JwtClaimNames.SUB,
                        JwtConfiguration::isUuid
                ),
                new JwtClaimValidator<String>(
                        JwtClaimNames.JTI,
                        jti -> jti != null && !jti.isBlank()
                )
        );
    }

    private static boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }

    private static RSAPublicKey readPublicKey(Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.x509().convert(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the JWT public key", exception);
        }
    }

    private static RSAPrivateKey readPrivateKey(Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the JWT private key", exception);
        }
    }
}
