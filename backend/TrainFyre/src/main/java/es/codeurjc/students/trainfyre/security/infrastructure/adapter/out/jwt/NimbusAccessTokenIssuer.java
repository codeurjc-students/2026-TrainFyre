package es.codeurjc.students.trainfyre.security.infrastructure.adapter.out.jwt;

import es.codeurjc.students.trainfyre.security.domain.AccessToken;
import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.infrastructure.JwtClaims;
import es.codeurjc.students.trainfyre.security.infrastructure.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class NimbusAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final SecurityProperties properties;
    private final Clock clock;

    @Override
    public AccessToken issue(AuthenticatedUser user) {
        Duration lifetime = properties.jwt().accessTokenTtl();
        Instant issuedAt = clock.instant();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .audience(List.of(properties.jwt().audience()))
                .subject(user.id().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(issuedAt.plus(lifetime))
                .id(UUID.randomUUID().toString())
                .claim(JwtClaims.ROLES, user.roles().stream().map(Role::name).sorted().toList())
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new AccessToken(token, lifetime);
    }
}
