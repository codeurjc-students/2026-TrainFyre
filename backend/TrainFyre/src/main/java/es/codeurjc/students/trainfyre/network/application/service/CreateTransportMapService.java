package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.CreateTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CreateTransportMapService implements CreateTransportMapUseCase {

    private final TransportMapPort transportMapPort;

    @CommandHandler
    @Override
    public UUID execute(CreateTransportMapCommand input) {

        transportMapPort.findBySource(input.mapMetadata().source()).ifPresent(exception -> {
            throw new MapAlreadyExistsException("Map with source: " + input.mapMetadata().source() + " already exists");
        });


        TransportMap transportMap = TransportMap.createTransportMap(input.mapMetadata(), input.lines(), input.now());

        transportMapPort.save(transportMap);

        return transportMap.getId().value();
    }
}
