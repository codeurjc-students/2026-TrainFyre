package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request;

import es.codeurjc.students.trainfyre.users.application.port.in.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = UserConstraints.USERNAME_MAX_LENGTH) String username,
        @NotBlank @Size(max = UserConstraints.PASSWORD_MAX_LENGTH) String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }

    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=****]";
    }
}
