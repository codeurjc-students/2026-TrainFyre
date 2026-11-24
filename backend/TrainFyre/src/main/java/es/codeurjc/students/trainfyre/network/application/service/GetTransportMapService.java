package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapQuery;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetTransportMapService implements GetTransportMapUseCase {

    private final MapResolver mapResolver;

    @Override
    public TransportMap execute(GetTransportMapQuery input) {
        return mapResolver.require(input.map());
    }
}
