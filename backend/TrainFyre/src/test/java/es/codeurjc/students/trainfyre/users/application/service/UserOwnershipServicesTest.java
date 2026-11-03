package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.UserAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserOwnershipServicesTest {

    private final UUID targetId = UUID.randomUUID();
    private final AuthenticatedUser anotherUser = new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER));

    @Mock
    private UserPort userPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void getByIdIsDeniedForAnotherUser() {
        var service = new GetUserByIdService(userPort);

        assertThatThrownBy(() -> service.execute(new GetUserByIdQuery(anotherUser, targetId)))
                .isInstanceOf(UserAccessDeniedException.class);

        verifyNoInteractions(userPort);
    }

    @Test
    void updateIsDeniedForAnotherUser() {
        var service = new UpdateUserService(userPort, passwordEncoder);
        var command = new UpdateUserCommand(anotherUser, targetId, "new-name", null, null, null);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(UserAccessDeniedException.class);

        verifyNoInteractions(userPort, passwordEncoder);
    }

    @Test
    void deleteIsDeniedForAnotherUser() {
        var service = new DeleteUserService(userPort);

        assertThatThrownBy(() -> service.execute(new DeleteUserCommand(anotherUser, targetId)))
                .isInstanceOf(UserAccessDeniedException.class);

        verifyNoInteractions(userPort);
    }
}
