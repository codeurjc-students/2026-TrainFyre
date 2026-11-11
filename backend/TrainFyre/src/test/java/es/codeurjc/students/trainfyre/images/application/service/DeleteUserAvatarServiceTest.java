package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteUserAvatarServiceTest {

    private final ImagePort imagePort = mock(ImagePort.class);

    private final DeleteUserAvatarService service = new DeleteUserAvatarService(imagePort);

    @Test
    void shouldDeleteImageWhenUserIsOwner() {
        // Given
        UUID userId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Delete command = new Delete(authenticatedUser, userId);

        // When
        service.execute(command);

        // Then
        verify(imagePort).delete(userId);
    }

    @Test
    void shouldDeleteImageWhenAdminUser() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID imageUuid = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.ADMIN));
        Delete command = new Delete(authenticatedUser, imageUuid);

        // When
        service.execute(command);

        // Then
        verify(imagePort).delete(imageUuid);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        // Given
        UUID userId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Delete command = new Delete(authenticatedUser, userId);

        RuntimeException persistenceFailure = new RuntimeException("Could not delete image");

        doThrow(persistenceFailure)
                .when(imagePort)
                .delete(userId);

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }
}
