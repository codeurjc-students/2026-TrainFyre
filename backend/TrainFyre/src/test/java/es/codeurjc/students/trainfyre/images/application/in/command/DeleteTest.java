package es.codeurjc.students.trainfyre.images.application.in.command;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final AuthenticatedUser DEFAULT_USER = new AuthenticatedUser(USER_ID, Set.of(Role.USER));

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
                () -> new Delete(DEFAULT_USER, null),
                () -> new Delete(null, USER_ID)
        );
    }

    @Test
    void shouldAllowDeleteWhenUserIsOwner() {
        Delete delete = new Delete(DEFAULT_USER, USER_ID);

        assertThatCode(delete::assertCanChangeAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowDeleteWhenUserIsAdmin() {
        UUID imageUuid = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Delete delete = new Delete(adminUser, imageUuid);

        assertThatCode(delete::assertCanChangeAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldDenyDeleteWhenUserIsNotOwnerAndNotAdmin() {
        UUID requesterId = UUID.randomUUID();
        Delete delete = new Delete(
                new AuthenticatedUser(requesterId, Set.of(Role.USER)),
                USER_ID
        );

        assertThrows(ChangeAvatarDeniedException.class, delete::assertCanChangeAvatar);
    }
}
