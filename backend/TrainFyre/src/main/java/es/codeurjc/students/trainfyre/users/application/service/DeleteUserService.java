package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.DeleteUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserPort userPort;

    @Override
    public Void execute(DeleteUserCommand input) {

        userPort.delete(input.id());

        return null;
    }
}
