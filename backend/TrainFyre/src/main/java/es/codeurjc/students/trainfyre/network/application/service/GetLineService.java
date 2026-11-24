package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetLineQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineId;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetLineService implements GetLineUseCase {

    private final MapResolver mapResolver;

    @Override
    public Line execute(GetLineQuery input) {
        TransportMap transportMap = mapResolver.require(input.map());

        return switch (input.line()) {
            case LineRef.ById(LineId id) -> transportMap.findLine(id);
            case LineRef.ByExternalId(String externalId) -> transportMap.findLine(externalId);
        };
    }
}
