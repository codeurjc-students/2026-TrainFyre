package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.command.CreateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    private final UserPort userPort = mock(UserPort.class);
    private final CreateUserService service = new CreateUserService(userPort);

    @Test
    void shouldSaveUserAndReturnItsId() {
        // Given
        CreateUserCommand command =
                new CreateUserCommand("pablo", "valid-password");

        // When
        UUID result = service.execute(command);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertAll(
                () -> assertNotNull(savedUser.getId()),
                () -> assertEquals(savedUser.getId(), result),
                () -> assertEquals(command.username(), savedUser.getUsername()),
                () -> assertEquals(command.password(), savedUser.getPassword()),
                () -> assertTrue(savedUser.getAlertSubscriptions().isEmpty())
        );
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        // Given
        CreateUserCommand command =
                new CreateUserCommand("pablo", "valid-password");

        RuntimeException persistenceFailure =
                new RuntimeException("Could not save user");

        doThrow(persistenceFailure)
                .when(userPort)
                .save(any(User.class));

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }
}
