package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteTransportMapService implements DeleteTransportMapUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;

    @CommandHandler
    @Override
    public Void execute(DeleteTransportMapCommand input) {
        transportMapPort.delete(mapResolver.require(input.map()).getId().value());

        return null;
    }
}
