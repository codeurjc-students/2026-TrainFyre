package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CreateIncidenceService implements CreateIncidenceUseCase{

    private final IncidencePort incidencePort;

    @CommandHandler
    @Override
    public UUID execute(CreateIncidenceCommand input) {

        Incidence incidence = Incidence.createIncidence(input.affectedNetwork(), input.occurrence(), input.description(), input.classification());

        incidencePort.save(incidence);

        return incidence.getId();

    }

}
