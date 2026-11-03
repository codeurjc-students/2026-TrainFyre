package es.codeurjc.students.trainfyre.users.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.security.domain.AccessToken;
import es.codeurjc.students.trainfyre.users.application.port.in.command.LoginCommand;

public interface LoginUseCase extends UseCase<LoginCommand, AccessToken> {
}
