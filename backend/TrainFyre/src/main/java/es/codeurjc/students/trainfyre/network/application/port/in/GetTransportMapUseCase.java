package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapQuery;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

/**
 * Returns the transport map identified by a {@link MapRef}.
 *
 * @throws MapNotFoundException if no map matches the reference
 */
public interface GetTransportMapUseCase extends UseCase<GetTransportMapQuery, TransportMap> {
}
