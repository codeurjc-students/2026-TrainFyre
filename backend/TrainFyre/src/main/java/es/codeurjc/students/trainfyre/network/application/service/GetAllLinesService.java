package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetAllLinesUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllLinesQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class GetAllLinesService implements GetAllLinesUseCase {

    private final MapResolver mapResolver;

    @Override
    public Collection<Line> execute(GetAllLinesQuery input) {
        return List.of();
    }
}
