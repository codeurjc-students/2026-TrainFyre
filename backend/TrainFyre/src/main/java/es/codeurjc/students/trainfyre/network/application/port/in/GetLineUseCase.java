package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetLineQuery;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.LineNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

/**
 * Returns the line identified by a {@link LineRef} in the map identified by
 * a {@link MapRef}.
 *
 * @throws MapNotFoundException if no map matches the reference
 * @throws LineNotFoundException if the map has no line matching the reference
 */
public interface GetLineUseCase extends UseCase<GetLineQuery, Line> {
}
