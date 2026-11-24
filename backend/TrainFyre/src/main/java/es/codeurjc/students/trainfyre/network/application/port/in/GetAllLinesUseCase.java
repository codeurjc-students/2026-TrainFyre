package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetAllLinesQuery;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

import java.util.Collection;

/**
 * Returns a snapshot of the lines of the map identified by a {@link MapRef},
 * in no particular order. Later changes of the map do not change it.
 *
 * @throws MapNotFoundException if no map matches the reference
 */
public interface GetAllLinesUseCase extends UseCase<GetAllLinesQuery, Collection<Line>> {
}
