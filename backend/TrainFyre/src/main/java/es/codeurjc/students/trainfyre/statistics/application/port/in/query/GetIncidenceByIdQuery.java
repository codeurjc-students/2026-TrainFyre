package es.codeurjc.students.trainfyre.statistics.application.port.in.query;

import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.UUID;

@QueryModel
public record GetIncidenceByIdQuery(UUID id) {
}
