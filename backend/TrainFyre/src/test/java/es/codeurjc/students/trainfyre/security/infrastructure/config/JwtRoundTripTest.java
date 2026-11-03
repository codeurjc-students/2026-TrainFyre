package es.codeurjc.students.trainfyre.security.infrastructure.config;

import es.codeurjc.students.trainfyre.security.domain.AccessToken;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.in.jwt.AuthenticatedUserJwtConverter;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.out.jwt.NimbusAccessTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtRoundTripTest {

    private final JwtConfiguration configuration = new JwtConfiguration();
    private final AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER));

    private SecurityProperties properties;
    private JwtEncoder encoder;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties(
                new SecurityProperties.Jwt(
                        "trainfyre",
                        "trainfyre-api",
                        Duration.ofMinutes(15),
                        "test-key",
                        new ClassPathResource("jwt/test-private.pem"),
                        new ClassPathResource("jwt/test-public.pem")
                ),
                new SecurityProperties.Cors(List.of("http://localhost:5173"))
        );

        JwtKeys keys = configuration.jwtKeys(properties);
        encoder = configuration.jwtEncoder(keys, properties);
        decoder = configuration.jwtDecoder(keys, properties);
    }

    @Test
    void issuedTokenIsDecodedIntoTheSameAuthenticatedUser() {
        AccessToken token = issuerAt(Clock.systemUTC()).issue(user);

        Jwt jwt = decoder.decode(token.value());
        Authentication authentication = new AuthenticatedUserJwtConverter().convert(jwt);

        assertThat(authentication.getPrincipal()).isEqualTo(user);
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(jwt.getHeaders()).containsEntry("kid", "test-key");
        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
    }

    @Test
    void expiredTokenIsRejected() {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        AccessToken token = issuerAt(past).issue(user);

        assertThatThrownBy(() -> decoder.decode(token.value())).isInstanceOf(BadJwtException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = issuerAt(Clock.systemUTC()).issue(user).value();
        String[] parts = token.split("\\.");
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 2) + (parts[1].endsWith("AA") ? "BB" : "AA");

        assertThatThrownBy(() -> decoder.decode(parts[0] + "." + tamperedPayload + "." + parts[2]))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    void tokenWithAnotherAudienceIsRejected() {
        SecurityProperties otherAudience = new SecurityProperties(
                new SecurityProperties.Jwt(
                        "trainfyre",
                        "another-api",
                        Duration.ofMinutes(15),
                        "test-key",
                        properties.jwt().privateKey(),
                        properties.jwt().publicKey()
                ),
                properties.cors()
        );
        AccessToken token = new NimbusAccessTokenIssuer(encoder, otherAudience, Clock.systemUTC()).issue(user);

        assertThatThrownBy(() -> decoder.decode(token.value())).isInstanceOf(BadJwtException.class);
    }

    private NimbusAccessTokenIssuer issuerAt(Clock clock) {
        return new NimbusAccessTokenIssuer(encoder, properties, clock);
    }
}
