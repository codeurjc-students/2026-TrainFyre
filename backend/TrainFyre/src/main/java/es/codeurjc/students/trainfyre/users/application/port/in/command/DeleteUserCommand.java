package es.codeurjc.students.trainfyre.users.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record DeleteUserCommand(UUID id) {

    public DeleteUserCommand {
        Objects.requireNonNull(id, "id should not be null");
    }
}
