package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class DeleteTransportMapBySourceService implements DeleteTransportMapBySourceUseCase {

    private final TransportMapPort transportMapPort;

    @Override
    @CommandHandler
    public Void execute(DeleteTransportMapBySourceCommand input) {
        return null;
    }
}
