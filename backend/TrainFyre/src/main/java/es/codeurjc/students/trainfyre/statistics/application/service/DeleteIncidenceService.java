package es.codeurjc.students.trainfyre.statistics.application.service;

import es.codeurjc.students.trainfyre.statistics.application.port.in.DeleteIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import org.jmolecules.architecture.cqrs.CommandHandler;

public class DeleteIncidenceService implements DeleteIncidenceUseCase {

    private IncidencePort incidencePort;

    public DeleteIncidenceService(IncidencePort incidencePort) {
        this.incidencePort = incidencePort;
    }

    @CommandHandler
    @Override
    public Void execute(DeleteIncidenceCommand input) {
        incidencePort.delete(input.id());
        return null;
    }
}
