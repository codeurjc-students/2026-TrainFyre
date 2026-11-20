package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateTransportMapBySourceService implements UpdateTransportMapBySourceUseCase {

    private final TransportMapPort transportMapPort;
    private final MapSourcePolicy mapSourcePolicy;

    @CommandHandler
    @Override
    public Void execute(UpdateTransportMapBySourceCommand input) {
        TransportMap transportMap = findBySource(input.source());
        DataSource newSource = input.mapMetadata().source();

        if (!newSource.equals(transportMap.getMapMetadata().source())) {
            mapSourcePolicy.requireUnusedSource(newSource);
        }

        transportMap.updateMetadata(input.mapMetadata(), input.now());
        transportMapPort.save(transportMap);

        return null;
    }

    private TransportMap findBySource(DataSource source) {
        return transportMapPort.findBySource(source)
                .orElseThrow(() -> new MapNotFoundException("no map with source " + source + " exists"));
    }
}
