package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.SliceResponse;
import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllTransportMapsSlicedQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

public interface GetAllTransportMapsSlicedUseCase extends UseCase<GetAllTransportMapsSlicedQuery, SliceResponse<TransportMap>> {
}
