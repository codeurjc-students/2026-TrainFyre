package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapByIdCommand;

public interface DeleteTransportMapByIdUseCase extends UseCase<DeleteTransportMapByIdCommand, Void> {
}
