package es.codeurjc.students.trainfyre.statistics.application.port.in;

import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;

public interface GetAllIncidencesPaginatedUseCase extends UseCase<GetAllIncidencesPaginatedQuery, PagedResponse<Incidence>> {
}
