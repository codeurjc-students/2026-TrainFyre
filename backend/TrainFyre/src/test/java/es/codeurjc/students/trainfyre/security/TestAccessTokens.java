package es.codeurjc.students.trainfyre.security;

import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;

import java.util.Set;
import java.util.UUID;

public final class TestAccessTokens {

    private TestAccessTokens() {
    }

    public static String bearerFor(AccessTokenIssuer issuer, UUID userId, Role... roles) {
        var user = new AuthenticatedUser(userId, Set.of(roles));
        return "Bearer " + issuer.issue(user).value();
    }

    public static String bearerForRandomUser(AccessTokenIssuer issuer) {
        return bearerFor(issuer, UUID.randomUUID(), Role.USER);
    }
}
