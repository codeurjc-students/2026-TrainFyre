package es.codeurjc.students.trainfyre.users.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.CreateUserCommand;

import java.util.UUID;

public interface CreateUserUseCase extends UseCase<CreateUserCommand, UUID> {
}
