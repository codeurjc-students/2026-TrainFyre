package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;

import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;

@Command
public record DeleteLineCommand(MapRef map, LineRef line, Instant now) {

    public DeleteLineCommand {
        Objects.requireNonNull(map, "map should not be null");
        Objects.requireNonNull(line, "line should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
