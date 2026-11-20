package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteTransportMapByIdService implements DeleteTransportMapByIdUseCase {

    private final TransportMapPort transportMapPort;

    @CommandHandler
    @Override
    public Void execute(DeleteTransportMapByIdCommand input) {
        transportMapPort.delete(input.id());

        return null;
    }
}
