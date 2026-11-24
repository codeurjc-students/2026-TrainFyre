package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public record GetLineQuery(MapRef map, LineRef line) {
}
