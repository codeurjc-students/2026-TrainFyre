package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.UpdateLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.LineId;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateLineService implements UpdateLineUseCase {

    private final TransportMapPort transportMapPort;
    private final MapResolver mapResolver;

    @CommandHandler
    @Override
    public Void execute(UpdateLineCommand input) {
        TransportMap map = mapResolver.require(input.map());
        switch (input.line()) {
            case LineRef.ById(LineId id) -> map.updateLine(id, input.changes(), input.now());
            case LineRef.ByExternalId(String externalId) -> map.updateLine(externalId, input.changes(), input.now());
        }
        transportMapPort.save(map);
        return null;
    }
}
