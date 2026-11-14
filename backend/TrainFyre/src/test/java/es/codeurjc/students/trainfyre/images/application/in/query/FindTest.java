package es.codeurjc.students.trainfyre.images.application.in.query;

import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import es.codeurjc.students.trainfyre.images.application.port.in.query.FindAvatarDeniedException;
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

class FindTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final AuthenticatedUser DEFAULT_USER = new AuthenticatedUser(USER_ID, Set.of(Role.USER));

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }

    static Stream<Executable> invalidNullArguments() {
        UUID uuid = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(uuid, Set.of());
        return Stream.of(
                () -> new Find(null, uuid),
                () -> new Find(user, null)
        );
    }

    @Test
    void shouldAllowFindWhenUserIsOwner() {
        Find find = new Find(DEFAULT_USER, USER_ID);

        assertThatCode(find::assertCanFindAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowFindWhenUserIsAdmin() {
        UUID imageUuid = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Find find = new Find(adminUser, imageUuid);

        assertThatCode(find::assertCanFindAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldDenyFindWhenUserIsNotOwnerAndNotAdmin() {
        UUID requesterId = UUID.randomUUID();
        Find find = new Find(
                new AuthenticatedUser(requesterId, Set.of(Role.USER)),
                USER_ID
        );

        assertThrows(FindAvatarDeniedException.class, find::assertCanFindAvatar);
    }
}
