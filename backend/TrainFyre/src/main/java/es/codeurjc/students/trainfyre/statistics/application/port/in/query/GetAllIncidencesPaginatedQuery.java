package es.codeurjc.students.trainfyre.statistics.application.port.in.query;

import es.codeurjc.students.trainfyre.common.Pageable;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public record GetAllIncidencesPaginatedQuery(Pageable pageable) {
}
