package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateLineService implements UpdateLineUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;

    @CommandHandler
    @Override
    public Void execute(UpdateLineCommand input) {
        return null;
    }
}
