package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UploadUserAvatarServiceTest {

    private final ImagePort imagePort = mock(ImagePort.class);

    private final UploadUserAvatarService service = new UploadUserAvatarService(imagePort);

    @Test
    void shouldSaveImageWhenUserIsOwner() {
        // Given
        UUID userId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Image image = new Image(userId, new ByteArrayInputStream("image content".getBytes()), 14, "image/png");

        Upload command = new Upload(authenticatedUser, image);

        // When
        service.execute(command);

        // Then
        verify(imagePort).save(any(Image.class));
    }

    @Test
    void shouldThrowChangeAvatarDeniedExceptionWhenUserIsNotOwnerAndNotAdmin() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID imageOwnerId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Image image = new Image(imageOwnerId, new ByteArrayInputStream("image content".getBytes()), 14, "image/png");

        Upload command = new Upload(authenticatedUser, image);

        // When / Then
        ChangeAvatarDeniedException thrown = assertThrows(
                ChangeAvatarDeniedException.class,
                () -> service.execute(command)
        );

        // Then
        assertEquals("In order to change avatar, id must be the same as user id:" + userId + " or user must have admin role",
                thrown.getMessage());
    }

    @Test
    void shouldSaveImageWhenAdminUser() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.ADMIN));
        Image image = new Image(imageId, new ByteArrayInputStream("image content".getBytes()), 14, "image/png");

        Upload command = new Upload(authenticatedUser, image);

        // When
        service.execute(command);

        // Then
        verify(imagePort).save(any(Image.class));
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        // Given
        UUID userId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Image image = new Image(userId, new ByteArrayInputStream("image content".getBytes()), 14, "image/png");

        Upload command = new Upload(authenticatedUser, image);

        RuntimeException persistenceFailure = new RuntimeException("Could not save image");

        doThrow(persistenceFailure)
                .when(imagePort)
                .save(any(Image.class));

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }
}
