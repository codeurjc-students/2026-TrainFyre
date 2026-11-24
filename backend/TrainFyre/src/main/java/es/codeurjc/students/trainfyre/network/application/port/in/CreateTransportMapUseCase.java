package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.domain.DuplicateLineException;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;

import java.util.UUID;

/**
 * Creates a transport map, with the lines of the command if it has any, and returns its generated id.
 *
 * @throws MapAlreadyExistsException if a map already has the source
 * @throws DuplicateLineException if two lines of the command have the same ids
 */
public interface CreateTransportMapUseCase extends UseCase<CreateTransportMapCommand, UUID> {
}
