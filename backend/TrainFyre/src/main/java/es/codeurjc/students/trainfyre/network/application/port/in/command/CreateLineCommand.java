package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import org.jmolecules.architecture.cqrs.Command;

import java.time.Instant;

@Command
public record CreateLineCommand(MapRef map, String externalId, LineName name, LineType type, LineColors colors, Instant now) {
}
