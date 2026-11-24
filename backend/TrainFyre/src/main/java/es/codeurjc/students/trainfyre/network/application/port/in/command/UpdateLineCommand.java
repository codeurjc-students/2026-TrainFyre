package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.LineChanges;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;

@Command
public record UpdateLineCommand(MapRef map, LineRef line, LineChanges changes, Instant now) {

    public UpdateLineCommand {
        Objects.requireNonNull(map, "map should not be null");
        Objects.requireNonNull(line, "line should not be null");
        Objects.requireNonNull(changes, "changes should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
