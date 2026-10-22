package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.GetUserByIdUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserPort userPort;

    @CommandHandler
    @Override
    public UserDetails execute(GetUserByIdQuery input) {

        User user = userPort.findById(input.id(), User.class);

        return new UserDetails(user.getUsername(), user.getAlertSubscriptions());

    }

}
