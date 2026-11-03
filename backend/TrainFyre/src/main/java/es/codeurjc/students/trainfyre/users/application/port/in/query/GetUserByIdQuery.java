package es.codeurjc.students.trainfyre.users.application.port.in.query;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record GetUserByIdQuery(AuthenticatedUser requester, UUID id) {

    public GetUserByIdQuery {
        Objects.requireNonNull(requester, "requester should not be null");
        Objects.requireNonNull(id, "id should not be null");
    }
}
