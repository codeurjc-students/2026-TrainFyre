package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
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


}
