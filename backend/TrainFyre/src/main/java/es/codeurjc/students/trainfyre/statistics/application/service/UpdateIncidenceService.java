package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.UpdateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UpdateIncidenceService implements UpdateIncidenceUseCase  {

    private final IncidencePort incidencePort;

    @CommandHandler
    @Override
    public Void execute(UpdateIncidenceCommand input) {

        Incidence incidence = incidencePort.findById(input.uuid(), Incidence.class);

        Optional.ofNullable(input.changeAffectedNetwork()).ifPresent(incidence::changeAffectedNetwork);
        Optional.ofNullable(input.changeOccurrence()).ifPresent(incidence::changeOccurrence);
        Optional.ofNullable(input.changeDescription()).ifPresent(incidence::changeDescription);
        Optional.ofNullable(input.changeClassification()).ifPresent(incidence::changeClassification);

        incidencePort.save(incidence);

        return null;
    }


}
