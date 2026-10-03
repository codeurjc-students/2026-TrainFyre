package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.GetIncidenceByIdUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.domain.IncidenceDetails;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetIncidenceByIdService implements GetIncidenceByIdUseCase {

    private final IncidencePort incidencePort;

    @Override
    public IncidenceDetails execute(GetIncidenceByIdQuery input) {

        Incidence incidence = incidencePort.findById(input.id(), Incidence.class);

        return incidence.getIncidenceDetails();
    }
}
