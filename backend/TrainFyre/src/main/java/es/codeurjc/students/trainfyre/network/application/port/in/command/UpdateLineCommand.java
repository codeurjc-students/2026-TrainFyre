package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.LineChanges;

import java.time.Instant;

public record UpdateLineCommand(MapRef map, LineRef line, LineChanges changes, Instant now) {
}
