package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.UUID;

@Command
public record UpdateTransportMapByIdCommand(UUID uuid, MapMetadata mapMetadata, Instant now) {
}
