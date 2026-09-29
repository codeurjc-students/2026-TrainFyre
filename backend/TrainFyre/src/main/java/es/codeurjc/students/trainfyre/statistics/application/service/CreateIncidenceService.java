package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.SaveIncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.architecture.hexagonal.Application;

import java.util.UUID;

@Application
public class CreateIncidenceService implements CreateIncidenceUseCase{

    private SaveIncidencePort saveIncidencePort;

    public CreateIncidenceService(SaveIncidencePort saveIncidencePort) {
        this.saveIncidencePort = saveIncidencePort;
    }

    @CommandHandler
    @Override
    public UUID execute(CreateIncidenceCommand input) {

        Incidence incidence = Incidence.createIncidence(input.affectedNetwork(), input.occurrence(), input.description(), input.classification());

        saveIncidencePort.save(incidence);

        return incidence.getId();

    }

}
