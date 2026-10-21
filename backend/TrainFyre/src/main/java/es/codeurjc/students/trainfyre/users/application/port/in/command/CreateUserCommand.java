package es.codeurjc.students.trainfyre.users.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

@Command
public record CreateUserCommand(String username, String password) {
}
