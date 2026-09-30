package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.GetIncidenceByIdUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.IncidenceDetails;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;

public class GetIncidenceByIdService implements GetIncidenceByIdUseCase {

    private IncidencePort incidencePort;

    public GetIncidenceByIdService(IncidencePort incidencePort) {
        this.incidencePort = incidencePort;
    }

    @Override
    public IncidenceDetails execute(GetIncidenceByIdQuery input) {
        return null;
    }
}
