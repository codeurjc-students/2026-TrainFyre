package es.codeurjc.students.trainfyre.users.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;

public interface UpdateUserUseCase extends UseCase<UpdateUserCommand, Void> {
}
