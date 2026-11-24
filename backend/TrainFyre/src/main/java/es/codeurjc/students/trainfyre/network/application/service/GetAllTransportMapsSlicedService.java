package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.common.SliceResponse;
import es.codeurjc.students.trainfyre.network.application.port.in.GetAllTransportMapsSlicedUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllTransportMapsSlicedQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetAllTransportMapsSlicedService implements GetAllTransportMapsSlicedUseCase {

    private final TransportMapPort transportMapPort;

    @CommandHandler
    @Override
    public SliceResponse<TransportMap> execute(GetAllTransportMapsSlicedQuery input) {
        return transportMapPort.findSlice(input.pageable());
    }
}
