package es.codeurjc.students.trainfyre.users.application.port.in.command;

import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateUserCommandTest {

    private static final UUID USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final AuthenticatedUser REQUESTER =
            new AuthenticatedUser(USER_ID, Set.of(Role.USER));

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new UpdateUserCommand(
                        REQUESTER,
                        null,
                        "new-username",
                        null,
                        List.of(),
                        List.of()
                )
        );
    }

    @ParameterizedTest(name = "[{index}] añadir={0}, eliminar={1}")
    @MethodSource("listsWithoutChanges")
    void shouldThrowNullPointerExceptionWhenNoChangesAreProvided(
            List<AlertSubscription> subscriptionsToAdd,
            List<AlertSubscription> subscriptionsToDelete
    ) {
        assertThrows(
                NullPointerException.class,
                () -> new UpdateUserCommand(
                        REQUESTER,
                        USER_ID,
                        null,
                        null,
                        subscriptionsToAdd,
                        subscriptionsToDelete
                )
        );
    }

    @ParameterizedTest(name = "[{index}] añadir={0}, eliminar={1}")
    @MethodSource("listsWithoutChanges")
    void shouldAcceptUsernameUpdateEvenWhenListsProvideNoChanges(
            List<AlertSubscription> subscriptionsToAdd,
            List<AlertSubscription> subscriptionsToDelete
    ) {
        assertDoesNotThrow(
                () -> new UpdateUserCommand(
                        REQUESTER,
                        USER_ID,
                        "new-username",
                        null,
                        subscriptionsToAdd,
                        subscriptionsToDelete
                )
        );
    }

    @ParameterizedTest(name = "[{index}] añadir={0}, eliminar={1}")
    @MethodSource("listsWithoutChanges")
    void shouldAcceptPasswordUpdateEvenWhenListsProvideNoChanges(
            List<AlertSubscription> subscriptionsToAdd,
            List<AlertSubscription> subscriptionsToDelete
    ) {
        assertDoesNotThrow(
                () -> new UpdateUserCommand(
                        REQUESTER,
                        USER_ID,
                        null,
                        "new-password",
                        subscriptionsToAdd,
                        subscriptionsToDelete
                )
        );
    }

    private static Stream<Arguments> listsWithoutChanges() {
        List<AlertSubscription> empty = List.of();

        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(empty, null),
                Arguments.of(null, empty),
                Arguments.of(empty, empty)
        );
    }
}
