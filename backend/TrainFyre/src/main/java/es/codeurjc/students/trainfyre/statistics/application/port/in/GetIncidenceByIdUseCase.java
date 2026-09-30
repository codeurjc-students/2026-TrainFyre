package es.codeurjc.students.trainfyre.statistics.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.IncidenceDetails;

public interface GetIncidenceByIdUseCase extends UseCase<GetIncidenceByIdQuery, IncidenceDetails> { }
