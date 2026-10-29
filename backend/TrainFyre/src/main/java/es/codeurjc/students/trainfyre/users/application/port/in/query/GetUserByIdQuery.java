package es.codeurjc.students.trainfyre.users.application.port.in.query;

import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record GetUserByIdQuery(UUID id) {

    public GetUserByIdQuery {
        Objects.requireNonNull(id, "id should not be null");
    }
}
