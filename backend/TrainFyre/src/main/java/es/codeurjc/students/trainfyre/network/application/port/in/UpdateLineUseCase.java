package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.LineNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

/**
 * Applies the changes to the line identified by a {@link LineRef} in the
 * map identified by a {@link MapRef}. Only the components of the changes that
 * are not {@code null} are applied.
 *
 * @throws MapNotFoundException if no map matches the reference
 * @throws LineNotFoundException if the map has no line matching the reference
 * @throws IllegalArgumentException if {@code now} is earlier than the last update of the map
 */
public interface UpdateLineUseCase extends UseCase<UpdateLineCommand, Void> {
}
