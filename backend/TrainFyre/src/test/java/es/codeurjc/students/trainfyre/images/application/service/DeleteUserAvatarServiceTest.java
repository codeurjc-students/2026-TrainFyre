package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteUserAvatarServiceTest {

    @Mock
    private ImagePort imagePort;

    private DeleteUserAvatarService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        service = new DeleteUserAvatarService(imagePort);
    }

    @Test
    void shouldDeleteImageWhenUserIsOwner() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Delete command = new Delete(authenticatedUser, USER_ID);

        service.execute(command);

        verify(imagePort).delete(USER_ID);
    }

    @Test
    void shouldDeleteImageWhenUserIsAdmin() {
        UUID imageUuid = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Delete command = new Delete(adminUser, imageUuid);

        service.execute(command);

        verify(imagePort).delete(imageUuid);
    }

    @Test
    void shouldDenyDeleteWhenUserIsNotOwnerAndNotAdmin() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Delete command = new Delete(authenticatedUser, USER_ID);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ChangeAvatarDeniedException.class)
                .hasMessageContaining("In order to change avatar");
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Delete command = new Delete(authenticatedUser, USER_ID);

        RuntimeException persistenceFailure = new RuntimeException("Could not delete image");
        doThrow(persistenceFailure).when(imagePort).delete(any(UUID.class));

        assertThatThrownBy(() -> service.execute(command))
                .isSameAs(persistenceFailure);
    }
}
