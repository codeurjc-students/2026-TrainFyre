package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateTransportMapService implements UpdateTransportMapUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;
    private final MapSourcePolicy mapSourcePolicy;

    @CommandHandler
    @Override
    public Void execute(UpdateTransportMapCommand input) {
        TransportMap transportMap = mapResolver.require(input.map());
        DataSource newSource = input.mapMetadata().source();

        if (!newSource.equals(transportMap.getMapMetadata().source())) {
            mapSourcePolicy.requireUnusedSource(newSource);
        }

        transportMap.updateMetadata(input.mapMetadata(), input.now());
        transportMapPort.save(transportMap);

        return null;
    }
}
