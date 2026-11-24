package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;

import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;

@Command
public record RemoveLineCommand(MapRef map, LineRef line, Instant now) {

    public RemoveLineCommand {
        Objects.requireNonNull(map, "map should not be null");
        Objects.requireNonNull(line, "line should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
