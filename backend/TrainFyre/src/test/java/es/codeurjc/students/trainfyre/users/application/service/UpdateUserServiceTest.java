package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final String ORIGINAL_USERNAME = "pablo";
    private static final String ORIGINAL_PASSWORD = "original-password";

    private final UserPort userPort = mock(UserPort.class);
    private final UpdateUserService service = new UpdateUserService(userPort);

    @ParameterizedTest(name = "[{index}] username={0}, password={1}")
    @MethodSource("credentialUpdates")
    void shouldUpdateProvidedCredentialsAndPreserveOtherFields(
            String username,
            String password,
            String expectedUsername,
            String expectedPassword
    ) {
        // Given
        AlertSubscription existingSubscription = subscription(1L);
        User user = existingUser(existingSubscription);

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID, username, password, null, null
        );

        // When
        Void result = service.execute(command);

        // Then
        assertNull(result);
        assertSavedUser(
                expectedUsername,
                expectedPassword,
                List.of(existingSubscription)
        );
    }

    @Test
    void shouldPreserveSubscriptionsWhenBothListsAreEmpty() {
        // Given
        AlertSubscription existingSubscription = subscription(1L);
        User user = existingUser(existingSubscription);

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID, "new-username", null, List.of(), List.of()
        );

        // When
        service.execute(command);

        // Then
        assertSavedUser(
                "new-username",
                ORIGINAL_PASSWORD,
                List.of(existingSubscription)
        );
    }

    @Test
    void shouldAddSubscriptionsAndPreserveExistingOnes() {
        // Given
        AlertSubscription existingSubscription = subscription(1L);
        AlertSubscription firstToAdd = subscription(2L);
        AlertSubscription secondToAdd = subscription(3L);
        User user = existingUser(existingSubscription);

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID,
                null,
                null,
                List.of(firstToAdd, secondToAdd),
                null
        );

        // When
        service.execute(command);

        // Then
        assertSavedUser(
                ORIGINAL_USERNAME,
                ORIGINAL_PASSWORD,
                List.of(existingSubscription, firstToAdd, secondToAdd)
        );
    }

    @Test
    void shouldDeleteSubscriptionsAndPreserveRemainingOnes() {
        // Given
        AlertSubscription firstToDelete = subscription(1L);
        AlertSubscription secondToDelete = subscription(2L);
        AlertSubscription remainingSubscription = subscription(3L);
        User user = existingUser(
                firstToDelete,
                secondToDelete,
                remainingSubscription
        );

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID,
                null,
                null,
                null,
                List.of(firstToDelete, secondToDelete)
        );

        // When
        service.execute(command);

        // Then
        assertSavedUser(
                ORIGINAL_USERNAME,
                ORIGINAL_PASSWORD,
                List.of(remainingSubscription)
        );
    }

    @Test
    void shouldUpdateCredentialsAndSubscriptionsTogether() {
        // Given
        AlertSubscription toDelete = subscription(1L);
        AlertSubscription toKeep = subscription(2L);
        AlertSubscription toAdd = subscription(3L);
        User user = existingUser(toDelete, toKeep);

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID,
                "new-username",
                "new-password",
                List.of(toAdd),
                List.of(toDelete)
        );

        // When
        service.execute(command);

        // Then
        assertSavedUser(
                "new-username",
                "new-password",
                List.of(toKeep, toAdd)
        );
    }

    @Test
    void shouldRejectDuplicateSubscriptionWithoutSaving() {
        // Given
        User user = existingUser(subscription(1L));

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        // Otra instancia con el mismo valor que la suscripción existente.
        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID,
                null,
                null,
                List.of(subscription(1L)),
                null
        );

        // When
        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command)
        );

        // Then
        verify(userPort, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDeletingSubscriptionNotOwnedByUserWithoutSaving() {
        // Given
        User user = existingUser(subscription(1L));

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID,
                null,
                null,
                null,
                List.of(subscription(2L))
        );

        // When
        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(command)
        );

        // Then
        verify(userPort, never()).save(any(User.class));
    }

    @Test
    void shouldPropagateExceptionWhenFindingUserFailsWithoutSaving() {
        // Given
        RuntimeException retrievalFailure =
                new RuntimeException("Could not find user");

        when(userPort.findById(USER_ID, User.class))
                .thenThrow(retrievalFailure);

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID, "new-username", null, null, null
        );

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(retrievalFailure, thrown);
        verify(userPort, never()).save(any(User.class));
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        // Given
        User user = existingUser();

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        RuntimeException persistenceFailure =
                new RuntimeException("Could not save user");

        doThrow(persistenceFailure)
                .when(userPort)
                .save(any(User.class));

        UpdateUserCommand command = new UpdateUserCommand(
                USER_ID, "new-username", null, null, null
        );

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(command)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }

    private void assertSavedUser(
            String expectedUsername,
            String expectedPassword,
            List<AlertSubscription> expectedSubscriptions
    ) {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        List<AlertSubscription> savedSubscriptions =
                savedUser.getAlertSubscriptions();

        assertAll(
                () -> assertEquals(USER_ID, savedUser.getId()),
                () -> assertEquals(expectedUsername, savedUser.getUsername()),
                () -> assertEquals(expectedPassword, savedUser.getPassword()),
                () -> assertEquals(
                        expectedSubscriptions.size(),
                        savedSubscriptions.size()
                ),
                () -> assertTrue(
                        savedSubscriptions.containsAll(expectedSubscriptions)
                )
        );
    }

    private static User existingUser(AlertSubscription... subscriptions) {
        return User.reconstitute(
                USER_ID,
                ORIGINAL_USERNAME,
                ORIGINAL_PASSWORD,
                new ArrayList<>(List.of(subscriptions))
        );
    }

    private static AlertSubscription subscription(long lineId) {
        return new AlertSubscription(
                Set.of(lineId),
                ZonedDateTime.parse("2026-10-19T08:00:00Z"),
                ZonedDateTime.parse("2026-10-19T10:00:00Z"),
                EnumSet.of(DayOfWeek.MONDAY),
                ZonedDateTime.parse("2026-12-31T23:59:59Z")
        );
    }

    private static Stream<Arguments> credentialUpdates() {
        return Stream.of(
                Arguments.of(
                        "new-username",
                        null,
                        "new-username",
                        ORIGINAL_PASSWORD
                ),
                Arguments.of(
                        null,
                        "new-password",
                        ORIGINAL_USERNAME,
                        "new-password"
                ),
                Arguments.of(
                        "new-username",
                        "new-password",
                        "new-username",
                        "new-password"
                )
        );
    }
}
