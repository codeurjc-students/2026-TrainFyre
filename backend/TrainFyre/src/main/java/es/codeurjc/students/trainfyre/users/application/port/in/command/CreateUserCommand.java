package es.codeurjc.students.trainfyre.users.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record CreateUserCommand(String username, String password) {

    public CreateUserCommand {
        Objects.requireNonNull(username, "usename should not be null");
        Objects.requireNonNull(password, "password should not be null");
    }
}
