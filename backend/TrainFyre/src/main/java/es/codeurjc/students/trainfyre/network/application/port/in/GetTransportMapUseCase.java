package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

public interface GetTransportMapUseCase extends UseCase<GetTransportMapQuery, TransportMap> {
}
