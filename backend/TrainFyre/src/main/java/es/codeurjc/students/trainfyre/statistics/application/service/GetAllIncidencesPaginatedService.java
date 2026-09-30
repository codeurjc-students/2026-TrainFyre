package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.GetAllIncidencesPaginatedUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;

public class GetAllIncidencesPaginatedService implements GetAllIncidencesPaginatedUseCase {

    private IncidencePort incidencePort;

    public GetAllIncidencesPaginatedService(IncidencePort incidencePort) {
        this.incidencePort = incidencePort;
    }

    @Override
    public PagedResponse<Incidence> execute(GetAllIncidencesPaginatedQuery input) {
        return null;
    }
}
