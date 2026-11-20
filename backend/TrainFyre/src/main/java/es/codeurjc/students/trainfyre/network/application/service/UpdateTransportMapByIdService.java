package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateTransportMapByIdService implements UpdateTransportMapByIdUseCase {

    private final TransportMapPort transportMapPort;
    private final MapSourcePolicy mapSourcePolicy;

    @CommandHandler
    @Override
    public Void execute(UpdateTransportMapByIdCommand input) {
        TransportMap transportMap = transportMapPort.findById(input.uuid(), TransportMap.class);
        DataSource newSource = input.mapMetadata().source();

        if (!newSource.equals(transportMap.getMapMetadata().source())) {
            mapSourcePolicy.requireUnusedSource(newSource);
        }

        transportMap.updateMetadata(input.mapMetadata(), input.now());
        transportMapPort.save(transportMap);

        return null;
    }

}
