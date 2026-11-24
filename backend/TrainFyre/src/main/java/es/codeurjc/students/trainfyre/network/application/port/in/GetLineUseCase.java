package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetLineQuery;
import es.codeurjc.students.trainfyre.network.domain.Line;

public interface GetLineUseCase extends UseCase<GetLineQuery, Line> {
}
