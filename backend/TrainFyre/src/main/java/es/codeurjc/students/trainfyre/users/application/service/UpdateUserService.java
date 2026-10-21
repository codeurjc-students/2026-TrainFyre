package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.UpdateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final UserPort userPort;

    @Override
    public Void execute(UpdateUserCommand input) {
        return null;
    }
}
