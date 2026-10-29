package es.codeurjc.students.trainfyre.users.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;
import java.util.Objects;

@ValueObject
public record UserDetails(String username, List<AlertSubscription> alertSubscriptions) {

    public UserDetails {
        Objects.requireNonNull(username, "username should not be null");
        Objects.requireNonNull(alertSubscriptions, "alertSubscriptions should not be null");

        alertSubscriptions = List.copyOf(alertSubscriptions);
    }
}
