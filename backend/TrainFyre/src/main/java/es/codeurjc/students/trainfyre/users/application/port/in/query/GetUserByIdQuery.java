package es.codeurjc.students.trainfyre.users.application.port.in.query;

import java.util.Objects;
import java.util.UUID;

public record GetUserByIdQuery(UUID id) {

    public GetUserByIdQuery {
        Objects.requireNonNull(id, "id should not be null");
    }
}
