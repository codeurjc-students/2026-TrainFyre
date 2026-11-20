package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapByIdQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetTransportMapByIdService implements GetTransportMapByIdUseCase {

    private final TransportMapPort transportMapPort;

    @CommandHandler
    @Override
    public TransportMap execute(GetTransportMapByIdQuery input) {
        return null;
    }
}
