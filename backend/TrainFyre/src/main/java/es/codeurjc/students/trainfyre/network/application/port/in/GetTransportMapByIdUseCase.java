package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapByIdQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

public interface GetTransportMapByIdUseCase extends UseCase<GetTransportMapByIdQuery, TransportMap> {
}
