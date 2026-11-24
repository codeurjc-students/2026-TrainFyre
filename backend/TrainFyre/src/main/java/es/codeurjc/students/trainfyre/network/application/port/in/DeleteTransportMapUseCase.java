package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapCommand;

public interface DeleteTransportMapUseCase extends UseCase<DeleteTransportMapCommand, Void> {
}
