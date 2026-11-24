package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.common.Pageable;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;

@QueryModel
public record GetAllTransportMapsSlicedQuery(Pageable pageable) {

    public GetAllTransportMapsSlicedQuery {
        Objects.requireNonNull(pageable, "pageable should not be null");
    }
}
