package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.DeleteIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteIncidenceService implements DeleteIncidenceUseCase {

    private final IncidencePort incidencePort;

    @CommandHandler
    @Override
    public Void execute(DeleteIncidenceCommand input) {
        incidencePort.delete(input.id());
        return null;
    }
}
