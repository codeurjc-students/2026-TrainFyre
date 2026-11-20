package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;

@Command
public record UpdateTransportMapBySourceCommand(DataSource source, MapMetadata mapMetadata, Instant now) {

    public UpdateTransportMapBySourceCommand {
        Objects.requireNonNull(source, "source should not be null");
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
