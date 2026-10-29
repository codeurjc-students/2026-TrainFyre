package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserServiceTest {

    private final UserPort userPort = mock(UserPort.class);
    private final DeleteUserService service = new DeleteUserService(userPort);

    @Test
    void shouldDeleteUserByIdAndReturnNull() {
        // Given
        UUID userId =
                UUID.fromString("73a06591-345c-4eae-9f49-dfeef592a148");

        DeleteUserCommand command = new DeleteUserCommand(userId);

        // When
        Void result = service.execute(command);

        // Then
        verify(userPort).delete(userId);
        assertNull(result);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        // Given
        UUID userId =
                UUID.fromString("73a06591-345c-4eae-9f49-dfeef592a148");

        DeleteUserCommand command = new DeleteUserCommand(userId);

        RuntimeException persistenceFailure =
                new RuntimeException("Could not delete user");

        when(userPort.delete(userId)).thenThrow(persistenceFailure);

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }
}
