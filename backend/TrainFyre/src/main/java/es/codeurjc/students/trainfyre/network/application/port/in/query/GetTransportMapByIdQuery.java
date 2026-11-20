package es.codeurjc.students.trainfyre.network.application.port.in.query;

import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record GetTransportMapByIdQuery(UUID uuid) {

    public GetTransportMapByIdQuery {
        Objects.requireNonNull(uuid, "id should not be null");
    }
}
