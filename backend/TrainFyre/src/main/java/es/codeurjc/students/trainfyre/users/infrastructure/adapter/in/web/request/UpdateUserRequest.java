package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateUserRequest(
        @Size(min = UserConstraints.USERNAME_MIN_LENGTH, max = UserConstraints.USERNAME_MAX_LENGTH)
        @Pattern(regexp = UserConstraints.USERNAME_PATTERN)
        String username,

        @Size(min = UserConstraints.PASSWORD_MIN_LENGTH, max = UserConstraints.PASSWORD_MAX_LENGTH)
        String password,

        List<AlertSubscription> alertSubscriptionListToAdd,
        List<AlertSubscription> alertSubscriptionListToDelete
) {

    @JsonIgnore
    @AssertTrue(message = "At least one modification must be provided")
    public boolean isModifying() {
        return username != null
                || password != null
                || (alertSubscriptionListToAdd != null && !alertSubscriptionListToAdd.isEmpty())
                || (alertSubscriptionListToDelete != null && !alertSubscriptionListToDelete.isEmpty());
    }

    public UpdateUserCommand toCommand(AuthenticatedUser requester, UUID id) {
        return new UpdateUserCommand(
                requester,
                id,
                username,
                password,
                alertSubscriptionListToAdd,
                alertSubscriptionListToDelete
        );
    }

    @Override
    public String toString() {
        return "UpdateUserRequest[username=" + username
                + ", password=" + (password == null ? null : "****")
                + ", alertSubscriptionListToAdd=" + alertSubscriptionListToAdd
                + ", alertSubscriptionListToDelete=" + alertSubscriptionListToDelete + "]";
    }
}
