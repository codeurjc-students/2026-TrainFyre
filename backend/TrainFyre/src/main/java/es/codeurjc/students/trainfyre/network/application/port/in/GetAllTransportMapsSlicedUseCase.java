package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.SliceResponse;
import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllTransportMapsSlicedQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;

/**
 * Returns the slice of transport maps at the page and size of the query. The result says whether another
 * slice follows, but not how many maps exist in total.
 */
public interface GetAllTransportMapsSlicedUseCase extends UseCase<GetAllTransportMapsSlicedQuery, SliceResponse<TransportMap>> {
}
