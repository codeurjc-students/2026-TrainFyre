package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.UpdateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import es.codeurjc.students.trainfyre.users.domain.UserAccessPolicy;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final UserPort userPort;

    private final PasswordEncoder passwordEncoder;

    @CommandHandler
    @Override
    public Void execute(UpdateUserCommand input) {
        UserAccessPolicy.assertCanManage(input.requester(), input.id());

        User user = userPort.findById(input.id(), User.class);

        if (input.username() != null) {
            user.changeUsername(input.username());
        }

        if (input.password() != null) {
            String passwordHash = passwordEncoder.encode(input.password());
            user.changePassword(passwordHash);
        }

        var subscriptionsToAdd = input.alertSubscriptionListToAdd();
        if (subscriptionsToAdd != null) {
            subscriptionsToAdd.forEach(user::addAlertSubscription);
        }

        var subscriptionsToDelete = input.alertSubscriptionListToDelete();
        if (subscriptionsToDelete != null) {
            subscriptionsToDelete.forEach(user::deleteAlertSubscription);
        }

        userPort.save(user);
        return null;
    }
}
