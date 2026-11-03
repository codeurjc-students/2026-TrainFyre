package es.codeurjc.students.trainfyre.users.application.port.in.command;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record DeleteUserCommand(AuthenticatedUser requester, UUID id) {

    public DeleteUserCommand {
        Objects.requireNonNull(requester, "requester should not be null");
        Objects.requireNonNull(id, "id should not be null");
    }
}
