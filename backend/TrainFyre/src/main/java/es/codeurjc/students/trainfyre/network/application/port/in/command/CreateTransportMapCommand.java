package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Command
public record CreateTransportMapCommand(MapMetadata mapMetadata, Collection<Line> lines, Instant now) {

    public CreateTransportMapCommand {
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(now, "now should not be null");
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
