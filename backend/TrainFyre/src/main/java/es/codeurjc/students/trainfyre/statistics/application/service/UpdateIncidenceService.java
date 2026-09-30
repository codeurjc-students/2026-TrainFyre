package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.UpdateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;

public class UpdateIncidenceService implements UpdateIncidenceUseCase  {

    private IncidencePort incidencePort;

    public UpdateIncidenceService(IncidencePort incidencePort){
        this.incidencePort = incidencePort;
    }

    @Override
    public Void execute(UpdateIncidenceCommand input) {
        return null;
    }


}
