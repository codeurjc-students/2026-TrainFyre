package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;

@QueryModel
public record GetTransportMapQuery(MapRef map) {

    public GetTransportMapQuery {
        Objects.requireNonNull(map, "map should not be null");
    }
}
