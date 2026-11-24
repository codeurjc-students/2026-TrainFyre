package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetLineUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetLineQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetLineService implements GetLineUseCase {

    private final MapResolver mapResolver;

    @Override
    public Line execute(GetLineQuery input) {
        return null;
    }
}
