package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;

import java.time.Instant;

public record RemoveLineCommand(MapRef map, LineRef line, Instant now) {
}
