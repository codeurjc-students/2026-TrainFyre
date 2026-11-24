package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;

/**
 * Deletes the map identified by a {@link MapRef} together with its lines.
 *
 * @throws MapNotFoundException if no map matches the reference
 */
public interface DeleteTransportMapUseCase extends UseCase<DeleteTransportMapCommand, Void> {
}
