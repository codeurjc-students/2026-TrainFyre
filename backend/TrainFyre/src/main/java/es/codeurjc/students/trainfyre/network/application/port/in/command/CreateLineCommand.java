package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;

@Command
public record CreateLineCommand(MapRef map, String externalId, LineName name, LineType type, LineColors colors, Instant now) {

    public CreateLineCommand {
        Objects.requireNonNull(map, "map should not be null");
        externalId = Line.normalizeExternalId(externalId);
        Objects.requireNonNull(name, "name should not be null");
        Objects.requireNonNull(type, "type should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
