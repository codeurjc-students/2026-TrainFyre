package es.codeurjc.students.trainfyre.users.application.port.in.command;

import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import org.jmolecules.architecture.cqrs.Command;

import java.util.List;

@Command
public record UpdateUserCommand(String username, String password, List<AlertSubscription> alertSubscriptionListToAdd, List<AlertSubscription> alertSubscriptionListToDelete) {
}
