package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request;

import es.codeurjc.students.trainfyre.users.application.port.in.command.CreateUserCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = UserConstraints.USERNAME_MIN_LENGTH, max = UserConstraints.USERNAME_MAX_LENGTH)
        @Pattern(regexp = UserConstraints.USERNAME_PATTERN)
        String username,

        @NotBlank
        @Size(min = UserConstraints.PASSWORD_MIN_LENGTH, max = UserConstraints.PASSWORD_MAX_LENGTH)
        String password
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(username, password);
    }

    @Override
    public String toString() {
        return "CreateUserRequest[username=" + username + ", password=****]";
    }
}
