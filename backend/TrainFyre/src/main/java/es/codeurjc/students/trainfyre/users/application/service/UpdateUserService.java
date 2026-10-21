package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.UpdateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import es.codeurjc.students.trainfyre.users.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final UserPort userPort;

    @Override
    public Void execute(UpdateUserCommand input) {

        User user = userPort.findById(input.id(), User.class);

        if(input.username() != null) user.changeUsername(input.username());
        if(input.password() != null) user.changePassword(input.password());
        if(input.alertSubscriptionListToAdd() != null && !input.alertSubscriptionListToAdd().isEmpty()){
            for(AlertSubscription alert : input.alertSubscriptionListToAdd()) user.addAlertSubscription(alert);
        }
        if(input.alertSubscriptionListToDelete() != null && ! input.alertSubscriptionListToDelete().isEmpty()){
            for(AlertSubscription alert : input.alertSubscriptionListToDelete()) user.deleteAlertSubscription(alert);
        }

        userPort.save(user);

        return null;
    }
}
