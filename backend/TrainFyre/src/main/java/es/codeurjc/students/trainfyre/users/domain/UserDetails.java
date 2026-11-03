package es.codeurjc.students.trainfyre.users.domain;

import es.codeurjc.students.trainfyre.security.domain.Role;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@ValueObject
public record UserDetails(String username, List<AlertSubscription> alertSubscriptions, Set<Role> roles) {

    public UserDetails {
        Objects.requireNonNull(username, "username should not be null");
        Objects.requireNonNull(alertSubscriptions, "alertSubscriptions should not be null");
        Objects.requireNonNull(roles, "roles should not be null");

        alertSubscriptions = List.copyOf(alertSubscriptions);
        roles = Set.copyOf(roles);
    }
}
