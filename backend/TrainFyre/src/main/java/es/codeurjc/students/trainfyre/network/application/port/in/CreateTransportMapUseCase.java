package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.CreateTransportMapCommand;

import java.util.UUID;

public interface CreateTransportMapUseCase extends UseCase<CreateTransportMapCommand, UUID> {
}
