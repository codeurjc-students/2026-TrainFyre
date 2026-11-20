package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateTransportMapByIdService implements UpdateTransportMapByIdUseCase {

    private final TransportMapPort transportMapPort;

    @CommandHandler
    @Override
    public Void execute(UpdateTransportMapByIdCommand input) {
        return null;
    }
}
