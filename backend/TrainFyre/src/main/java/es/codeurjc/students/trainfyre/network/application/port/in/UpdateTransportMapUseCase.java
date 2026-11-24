package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

/**
 * Replaces the whole metadata of the map identified by a {@link MapRef}.
 * A new source is only accepted when no other map has it.
 *
 * @throws MapNotFoundException if no map matches the reference
 * @throws MapAlreadyExistsException if another map already has the new source
 * @throws IllegalArgumentException if {@code now} is earlier than the last update of the map
 */
public interface UpdateTransportMapUseCase extends UseCase<UpdateTransportMapCommand, Void> {
}
