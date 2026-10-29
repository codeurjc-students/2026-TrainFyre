package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.CreateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.CreateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final UserPort userPort;

    @CommandHandler
    @Override
    public UUID execute(CreateUserCommand input) {

        User user = User.createUser(input.username(), input.password());

        userPort.save(user);

        return user.getId();

    }
}
