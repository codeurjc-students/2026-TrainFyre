package es.codeurjc.students.trainfyre.users.application.port.in.command;

import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import org.jmolecules.architecture.cqrs.Command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Command
public record UpdateUserCommand(UUID id, String username, String password, List<AlertSubscription> alertSubscriptionListToAdd, List<AlertSubscription> alertSubscriptionListToDelete) {

    public UpdateUserCommand {
        Objects.requireNonNull(id, "id should not be null");
        if (username == null
                && password == null
                && (alertSubscriptionListToAdd == null || alertSubscriptionListToAdd.isEmpty())
                && (alertSubscriptionListToDelete == null || alertSubscriptionListToDelete.isEmpty())) {
            throw new NullPointerException("At least one modification must be provided");
        }
    }
}
