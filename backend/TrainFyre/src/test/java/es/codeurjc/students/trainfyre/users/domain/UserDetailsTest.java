package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDetailsTest {

    @ParameterizedTest
    @MethodSource("nullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(
            String username,
            List<AlertSubscription> alertSubscriptions
    ) {
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> new UserDetails(username, alertSubscriptions)
        );
    }

    private static Stream<Arguments> nullArguments() {
        return Stream.of(
                Arguments.of(null, List.<AlertSubscription>of()),
                Arguments.of("pablo", null),
                Arguments.of(null, null)
        );
    }

    @Test
    void shouldKeepSubscriptionsImmutable() {
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");
        var subscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusWeeks(1)
        );
        var originalSubscriptions = new ArrayList<>(List.of(subscription));
        var details = new UserDetails("pablo", originalSubscriptions);

        originalSubscriptions.clear();

        assertEquals(List.of(subscription), details.alertSubscriptions());

        assertThrows(
                UnsupportedOperationException.class,
                () -> details.alertSubscriptions().clear()
        );

        assertEquals(List.of(subscription), details.alertSubscriptions());
    }

}
