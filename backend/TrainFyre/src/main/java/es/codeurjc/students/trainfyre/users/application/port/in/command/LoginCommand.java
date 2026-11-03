package es.codeurjc.students.trainfyre.users.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record LoginCommand(String username, String password) {

    public LoginCommand {
        Objects.requireNonNull(username, "username should not be null");
        Objects.requireNonNull(password, "password should not be null");
    }

    @Override
    public String toString() {
        return "LoginCommand[username=" + username + ", password=****]";
    }
}
