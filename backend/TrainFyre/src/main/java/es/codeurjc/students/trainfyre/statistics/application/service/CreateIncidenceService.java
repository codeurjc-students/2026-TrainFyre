package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.SaveIncidencePort;
import org.jmolecules.architecture.hexagonal.Application;

import java.util.UUID;

@Application
public class CreateIncidenceService implements CreateIncidenceUseCase{

    private SaveIncidencePort saveIncidencePort;

    public CreateIncidenceService(SaveIncidencePort saveIncidencePort) {
        this.saveIncidencePort = saveIncidencePort;
    }

    @Override
    public UUID execute(CreateIncidenceCommand input) {
        return null;
    }

}
