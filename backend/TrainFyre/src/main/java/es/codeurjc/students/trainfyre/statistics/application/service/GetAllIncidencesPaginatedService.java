package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.GetAllIncidencesPaginatedUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetAllIncidencesPaginatedService implements GetAllIncidencesPaginatedUseCase {

    private final IncidencePort incidencePort;

    @Override
    public PagedResponse<Incidence> execute(GetAllIncidencesPaginatedQuery input) {
        return incidencePort.findAll(input.pageable());
    }
}
