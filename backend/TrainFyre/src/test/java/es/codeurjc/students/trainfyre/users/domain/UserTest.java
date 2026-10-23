package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullCredentials")
    void createUserShouldThrowNullPointerExceptionWhenCredentialsAreNull(
            String scenario,
            String username,
            String password
    ) {
        assertThrows(NullPointerException.class, () -> User.createUser(username, password));
    }

    private static Stream<Arguments> nullCredentials() {
        return Stream.of(
                Arguments.of("username null", null, "valid-password"),
                Arguments.of("password null", "pablo", null),
                Arguments.of("ambos null", null, null)
        );
    }

    @Test
    void shouldAddAlertSubscription() {
        var user = User.createUser("pablo", "valid-password");
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );

        user.addAlertSubscription(subscription);

        assertEquals(List.of(subscription), user.getAlertSubscriptions());
    }

    @Test
    void shouldRejectNullSubscription() {
        var user = User.createUser("pablo", "valid-password");

        assertThrows(
                NullPointerException.class,
                () -> user.addAlertSubscription(null)
        );

        assertTrue(user.getAlertSubscriptions().isEmpty());
    }

    @Test
    void changeUsernameShouldThrowNullPointerExceptionWhenUsernameIsNull() {
        var user = User.createUser("pablo", "valid-password");

        assertThrows(
                NullPointerException.class,
                () -> user.changeUsername(null)
        );
    }

    @Test
    void changeUsernameShouldUpdateUsername() {
        var user = User.createUser("pablo", "valid-password");

        user.changeUsername("nuevo-username");

        assertEquals("nuevo-username", user.getUsername());
    }

    @Test
    void changePasswordShouldThrowNullPointerExceptionWhenPasswordIsNull() {
        var user = User.createUser("pablo", "valid-password");

        assertThrows(
                NullPointerException.class,
                () -> user.changePassword(null)
        );
    }

    @Test
    void changePasswordShouldUpdatePassword() {
        var user = User.createUser("pablo", "valid-password");

        user.changePassword("new-password");

        assertEquals("new-password", user.getPassword());
    }

    @Test
    void deleteAlertSubscriptionShouldThrowNullPointerExceptionWhenSubscriptionIsNull() {
        var user = User.createUser("pablo", "valid-password");

        assertThrows(
                NullPointerException.class,
                () -> user.deleteAlertSubscription(null)
        );
    }

    @Test
    void deleteAlertSubscriptionShouldRemoveSubscription() {
        var user = User.createUser("pablo", "valid-password");
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        user.addAlertSubscription(subscription);

        user.deleteAlertSubscription(subscription);

        assertTrue(user.getAlertSubscriptions().isEmpty());
    }

    @Test
    void deleteAlertSubscriptionShouldThrowIllegalArgumentExceptionWhenSubscriptionDoesNotExist() {
        var user = User.createUser("pablo", "valid-password");
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> user.deleteAlertSubscription(subscription)
        );
    }


    @Test
    void addAlertSubscriptionShouldThrowIllegalArgumentExceptionWhenSubscriptionAlreadyExists() {
        var user = User.createUser("pablo", "valid-password");
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        var duplicate = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        user.addAlertSubscription(subscription);

        assertThrows(
                IllegalArgumentException.class,
                () -> user.addAlertSubscription(duplicate)
        );

        assertEquals(List.of(subscription), user.getAlertSubscriptions());
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullReconstitutionArguments")
    void reconstituteShouldThrowNullPointerExceptionWhenArgumentIsNull(
            String scenario,
            UUID id,
            String username,
            String password,
            List<AlertSubscription> alertSubscriptions
    ) {
        assertThrows(
                NullPointerException.class,
                () -> User.reconstitute(id, username, password, alertSubscriptions)
        );
    }

    private static Stream<Arguments> nullReconstitutionArguments() {
        var id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        List<AlertSubscription> subscriptions = List.of();

        return Stream.of(
                Arguments.of("id null", null, "pablo", "valid-password", subscriptions),
                Arguments.of("username null", id, null, "valid-password", subscriptions),
                Arguments.of("password null", id, "pablo", null, subscriptions),
                Arguments.of("alertSubscriptions null", id, "pablo", "valid-password", null)
        );
    }

    @Test
    void reconstituteShouldRestoreUserState() {
        var id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var username = "pablo";
        var password = "valid-password";
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        var subscriptions = List.of(subscription);

        var user = User.reconstitute(id, username, password, subscriptions);

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(subscriptions, user.getAlertSubscriptions());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "original list changes do not affect user",
            "returned list cannot be modified",
            "immutable input still allows aggregate operations"
    })
    void shouldProtectSubscriptionsFromExternalMutation(String scenario) {
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        List<AlertSubscription> subscriptions =
                scenario.equals("immutable input still allows aggregate operations")
                        ? List.of(subscription)
                        : new java.util.ArrayList<>(List.of(subscription));

        var user = User.reconstitute(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "pablo",
                "valid-password",
                subscriptions
        );

        switch (scenario) {
            case "original list changes do not affect user" -> {
                subscriptions.clear();

                assertEquals(List.of(subscription), user.getAlertSubscriptions());

                user.deleteAlertSubscription(subscription);

                assertTrue(subscriptions.isEmpty());

                user.addAlertSubscription(subscription);

                assertTrue(subscriptions.isEmpty());
                assertEquals(List.of(subscription), user.getAlertSubscriptions());
            }
            case "returned list cannot be modified" -> {
                var exposedSubscriptions = user.getAlertSubscriptions();

                assertAll(
                        () -> assertThrows(
                                UnsupportedOperationException.class,
                                () -> exposedSubscriptions.add(subscription)
                        ),
                        () -> assertThrows(
                                UnsupportedOperationException.class,
                                () -> exposedSubscriptions.remove(0)
                        ),
                        () -> assertThrows(
                                UnsupportedOperationException.class,
                                () -> exposedSubscriptions.set(0, subscription)
                        ),
                        () -> assertThrows(
                                UnsupportedOperationException.class,
                                exposedSubscriptions::clear
                        )
                );

                assertEquals(List.of(subscription), user.getAlertSubscriptions());
            }
            case "immutable input still allows aggregate operations" -> {
                user.deleteAlertSubscription(subscription);

                assertTrue(user.getAlertSubscriptions().isEmpty());

                user.addAlertSubscription(subscription);

                assertEquals(List.of(subscription), user.getAlertSubscriptions());
                assertEquals(List.of(subscription), subscriptions);
            }
            default -> fail("Unexpected scenario: " + scenario);
        }
    }




}
