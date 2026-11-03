package es.codeurjc.students.trainfyre.security.infrastructure.adapter.in.jwt;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.infrastructure.JwtClaims;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@PrimaryAdapter
@Component
public class AuthenticatedUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        AuthenticatedUser user = new AuthenticatedUser(userId, readRoles(jwt));

        return new AuthenticatedUserAuthentication(user, jwt);
    }

    private Set<Role> readRoles(Jwt jwt) {
        List<String> names = jwt.getClaimAsStringList(JwtClaims.ROLES);

        if (names == null) {
            return Set.of();
        }

        return names.stream()
                .map(Role::find)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}
