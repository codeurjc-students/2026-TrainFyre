package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapBySourceQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

public interface GetTransportMapBySourceUseCase extends UseCase<GetTransportMapBySourceQuery, TransportMap> {
}
