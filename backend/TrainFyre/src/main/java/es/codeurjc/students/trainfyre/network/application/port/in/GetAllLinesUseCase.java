package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllLinesQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;

import java.util.Collection;

public interface GetAllLinesUseCase extends UseCase<GetAllLinesQuery, Collection<Line>> {
}
