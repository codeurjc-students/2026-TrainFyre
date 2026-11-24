package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;

@QueryModel
public record GetLineQuery(MapRef map, LineRef line) {

    public GetLineQuery {
        Objects.requireNonNull(map, "map should not be null");
        Objects.requireNonNull(line, "line should not be null");
    }
}
