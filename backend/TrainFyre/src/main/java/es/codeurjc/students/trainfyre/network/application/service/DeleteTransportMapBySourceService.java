package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
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
        TransportMap transportMap = findBySource(input.source());
        transportMapPort.delete(transportMap.getId().value());
        return null;
    }

    private TransportMap findBySource(DataSource source) {
        return transportMapPort.findBySource(source)
                .orElseThrow(() -> new MapNotFoundException("no map with source " + source + " exists"));
    }
}
