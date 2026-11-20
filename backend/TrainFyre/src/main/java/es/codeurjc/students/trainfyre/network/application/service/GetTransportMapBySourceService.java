package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapBySourceQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetTransportMapBySourceService implements GetTransportMapBySourceUseCase {

    private final TransportMapPort transportMapPort;

    @Override
    public TransportMap execute(GetTransportMapBySourceQuery input) {

        return findBySource(input.source());

    }

    private TransportMap findBySource(DataSource source) {
        return transportMapPort.findBySource(source)
                .orElseThrow(() -> new MapNotFoundException("no map with source " + source + " exists"));
    }
}
