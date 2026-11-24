package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.CreateLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateLineService implements CreateLineUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;

    @CommandHandler
    @Override
    public Void execute(CreateLineCommand input) {

        TransportMap transportMap = mapResolver.require(input.map());
        transportMap.addLine(Line.createLine(input.externalId(), input.name(), input.type(), input.colors()), input.now());
        transportMapPort.save(transportMap);
        return null;

    }
}
