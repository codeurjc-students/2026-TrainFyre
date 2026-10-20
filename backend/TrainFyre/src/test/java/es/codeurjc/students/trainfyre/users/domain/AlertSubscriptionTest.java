package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertSubscriptionTest {

    @ParameterizedTest(name = "[{index}] {0} null")
    @MethodSource("nullFields")
    void shouldRejectNullFields(
            String field,
            Set<Long> lines,
            ZonedDateTime start,
            ZonedDateTime end,
            EnumSet<DayOfWeek> days,
            ZonedDateTime expiration
    ) {
        assertThrows(NullPointerException.class, () -> new AlertSubscription(lines, start, end, days, expiration));
    }

    private static Stream<Arguments> nullFields() {
        var lines = Set.of(1L);
        var start = ZonedDateTime.parse("2026-10-20T08:00:00Z");
        var end = start.plusHours(1);
        var days = EnumSet.of(DayOfWeek.TUESDAY);
        var expiration = start.plusMonths(1);

        return Stream.of(
                Arguments.of("lines", null, start, end, days, expiration),
                Arguments.of("startTime", lines, null, end, days, expiration),
                Arguments.of("endTime", lines, start, null, days, expiration),
                Arguments.of("daysOfTheWeek", lines, start, end, null, expiration),
                Arguments.of("expiration", lines, start, end, days, null)
        );
    }
}

