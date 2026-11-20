package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;
import java.util.Collection;

@Command
public record CreateTransportMapCommand(MapMetadata mapMetadata, Collection<Line> lines, Instant now) {
}
