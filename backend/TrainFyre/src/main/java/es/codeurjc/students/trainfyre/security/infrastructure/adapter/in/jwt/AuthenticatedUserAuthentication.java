package es.codeurjc.students.trainfyre.security.infrastructure.adapter.in.jwt;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class AuthenticatedUserAuthentication extends AbstractAuthenticationToken {

    private final AuthenticatedUser principal;
    private final Jwt credentials;

    public AuthenticatedUserAuthentication(AuthenticatedUser principal, Jwt credentials) {
        super(principal.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList());
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public AuthenticatedUser getPrincipal() {
        return principal;
    }

    @Override
    public Jwt getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return principal.id().toString();
    }
}
