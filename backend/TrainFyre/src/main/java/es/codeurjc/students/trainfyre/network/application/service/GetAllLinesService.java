package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetAllLinesUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllLinesQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@AllArgsConstructor
public class GetAllLinesService implements GetAllLinesUseCase {

    private final MapResolver mapResolver;

    @Override
    public Collection<Line> execute(GetAllLinesQuery input) {
        return mapResolver.require(input.map()).lines();
    }
}
