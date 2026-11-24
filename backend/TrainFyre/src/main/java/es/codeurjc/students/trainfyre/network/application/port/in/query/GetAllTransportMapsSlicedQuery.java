package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.common.Pageable;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public record GetAllTransportMapsSlicedQuery(Pageable pageable) {
}
