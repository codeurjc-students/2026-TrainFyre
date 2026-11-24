package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteLineService implements DeleteLineUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;

    @CommandHandler
    @Override
    public Void execute(DeleteLineCommand input) {
        return null;
    }
}
