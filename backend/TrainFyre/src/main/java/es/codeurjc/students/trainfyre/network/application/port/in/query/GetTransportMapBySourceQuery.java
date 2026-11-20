package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;

@QueryModel
public record GetTransportMapBySourceQuery(DataSource source) {

    public GetTransportMapBySourceQuery {
        Objects.requireNonNull(source, "source should not be null");
    }
}
