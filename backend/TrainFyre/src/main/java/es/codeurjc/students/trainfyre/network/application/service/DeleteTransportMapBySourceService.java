package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteTransportMapBySourceService implements DeleteTransportMapBySourceUseCase {

    private final TransportMapPort transportMapPort;
    private final MapSourcePolicy mapSourcePolicy;

    @Override
    @CommandHandler
    public Void execute(DeleteTransportMapBySourceCommand input) {
        TransportMap transportMap = mapSourcePolicy.requireExistingMap(input.source());
        transportMapPort.delete(transportMap.getId().value());
        return null;
    }
}
