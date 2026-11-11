package es.codeurjc.students.trainfyre.images.application.in.command;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteTest {

    @Test
    void shouldThrowNullPointerExceptionWhenUuidIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Delete(
                        new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER)), null
                )
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAuthenticatedUserIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Delete(
                        null,
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void shouldThrowChangeAvatarDeniedExceptionWhenAuthenticatedUserIsNotOwnerOrAdmin() {
        UUID requesterId = UUID.randomUUID();
        UUID imageOwnerId = UUID.randomUUID();
        Delete delete = new Delete(
                new AuthenticatedUser(requesterId, Set.of(Role.USER)),
                imageOwnerId
        );

        assertThrows(ChangeAvatarDeniedException.class, delete::assertCanChangeAvatar);
    }
}
