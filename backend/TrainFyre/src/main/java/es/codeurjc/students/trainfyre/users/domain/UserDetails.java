package es.codeurjc.students.trainfyre.users.domain;

import java.util.List;

public record UserDetails(String username, List<AlertSubscription> alertSubscriptions) {
}
