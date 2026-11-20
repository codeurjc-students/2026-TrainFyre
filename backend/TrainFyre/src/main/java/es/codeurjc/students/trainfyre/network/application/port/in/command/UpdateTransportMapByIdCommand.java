package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Command
public record UpdateTransportMapByIdCommand(UUID uuid, MapMetadata mapMetadata, Instant now) {

    public UpdateTransportMapByIdCommand {
        Objects.requireNonNull(uuid, "uuid should not be null");
        Objects.requireNonNull(mapMetadata, "map metadata should not be null");
        Objects.requireNonNull(now, "now should not be null");
    }
}
