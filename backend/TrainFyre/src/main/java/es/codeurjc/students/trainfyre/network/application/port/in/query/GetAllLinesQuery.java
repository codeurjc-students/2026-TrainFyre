package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public record GetAllLinesQuery(MapRef map) {
}
