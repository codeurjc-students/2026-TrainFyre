package es.codeurjc.students.trainfyre.users.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;

public interface DeleteUserUseCase extends UseCase<DeleteUserCommand, Void> {
}
