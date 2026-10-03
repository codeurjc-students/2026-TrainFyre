package es.codeurjc.students.trainfyre.statistics.application.port.in.query;

import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record GetIncidenceByIdQuery(UUID id) {

    public GetIncidenceByIdQuery {
        Objects.requireNonNull(id, "id should not be null");
    }
}
