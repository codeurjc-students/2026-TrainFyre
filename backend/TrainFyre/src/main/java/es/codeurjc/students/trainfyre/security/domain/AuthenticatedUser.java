package es.codeurjc.students.trainfyre.security.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@ValueObject
public record AuthenticatedUser(UUID id, Set<Role> roles) {

    public AuthenticatedUser {
        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(roles, "roles should not be null");

        roles = Set.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean isOwner(UUID resourceOwnerId) {
        return id.equals(resourceOwnerId);
    }
}
