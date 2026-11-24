package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateLineCommand;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DuplicateLineException;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

/**
 * Adds a new line to the map identified by a {@link MapRef}. The line gets
 * its {@code LineId} when the map is persisted, so it is identified by its external id until then.
 *
 * @throws MapNotFoundException if no map matches the reference
 * @throws DuplicateLineException if the map already has a line with that external id
 * @throws IllegalArgumentException if {@code now} is earlier than the last update of the map
 */
public interface CreateLineUseCase extends UseCase<CreateLineCommand, Void> {
}
