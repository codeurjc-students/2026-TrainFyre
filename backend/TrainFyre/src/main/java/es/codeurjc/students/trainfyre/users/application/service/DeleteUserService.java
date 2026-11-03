package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.DeleteUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.UserAccessPolicy;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserPort userPort;

    @CommandHandler
    @Override
    public Void execute(DeleteUserCommand input) {

        UserAccessPolicy.assertCanManage(input.requester(), input.id());

        userPort.delete(input.id());

        return null;
    }
}
