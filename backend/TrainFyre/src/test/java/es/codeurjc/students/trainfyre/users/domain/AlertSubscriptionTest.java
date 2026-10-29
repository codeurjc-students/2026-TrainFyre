package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlertSubscriptionTest {

    private final AlertSubscription subscription = new AlertSubscription(
            Set.of(1L, 2L),
            ZonedDateTime.parse("2026-10-19T08:00:00Z"),
            ZonedDateTime.parse("2026-10-19T10:00:00Z"),
            EnumSet.of(DayOfWeek.MONDAY),
            ZonedDateTime.parse("2026-10-26T09:00:00Z")
    );

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
        assertThrows(
                NullPointerException.class,
                () -> new AlertSubscription(
                        lines, start, end, days, expiration
                )
        );
    }

    private static Stream<Arguments> nullFields() {
        var lines = Set.of(1L);
        var start = ZonedDateTime.parse("2026-10-20T08:00:00Z");
        var end = start.plusHours(1);
        var days = EnumSet.of(DayOfWeek.TUESDAY);
        var expiration = start.plusMonths(1);

        return Stream.of(
                Arguments.of(
                        "lines", null, start, end, days, expiration
                ),
                Arguments.of(
                        "startTime", lines, null, end, days, expiration
                ),
                Arguments.of(
                        "endTime", lines, start, null, days, expiration
                ),
                Arguments.of(
                        "daysOfTheWeek", lines, start, end, null, expiration
                ),
                Arguments.of(
                        "expiration", lines, start, end, days, null
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("affectedCases")
    void shouldDetermineWhetherSubscriptionIsAffected(
            String scenario,
            List<Long> lines,
            String timestamp,
            boolean expected
    ) {
        assertEquals(
                expected,
                subscription.isAffected(
                        lines,
                        ZonedDateTime.parse(timestamp)
                )
        );
    }

    private static Stream<Arguments> affectedCases() {
        return Stream.of(
                Arguments.of("primera línea suscrita",
                        List.of(1L), "2026-10-19T09:00:00Z", true),
                Arguments.of("segunda línea suscrita",
                        List.of(2L), "2026-10-19T09:00:00Z", true),
                Arguments.of("basta una línea coincidente",
                        List.of(3L, 2L), "2026-10-19T09:00:00Z", true),
                Arguments.of("ninguna línea coincidente",
                        List.of(3L), "2026-10-19T09:00:00Z", false),
                Arguments.of("lista de líneas vacía",
                        List.of(), "2026-10-19T09:00:00Z", false),
                Arguments.of("antes del inicio",
                        List.of(1L), "2026-10-19T07:59:59Z", false),
                Arguments.of("inicio incluido",
                        List.of(1L), "2026-10-19T08:00:00Z", true),
                Arguments.of("final incluido",
                        List.of(1L), "2026-10-19T10:00:00Z", true),
                Arguments.of("después del final",
                        List.of(1L), "2026-10-19T10:00:01Z", false),
                Arguments.of("día no suscrito",
                        List.of(1L), "2026-10-20T09:00:00Z", false),
                Arguments.of("recurrencia la siguiente semana",
                        List.of(1L), "2026-10-26T08:30:00Z", true),
                Arguments.of("expiración incluida",
                        List.of(1L), "2026-10-26T09:00:00Z", true),
                Arguments.of("suscripción expirada dentro de la franja",
                        List.of(1L), "2026-10-26T09:00:01Z", false)
        );
    }

    @Test
    void shouldNotChangeWhenOriginalLinesAreModified() {
        var originalLines = new HashSet<>(Set.of(1L, 2L));
        var actual = subscriptionWith(
                originalLines,
                EnumSet.of(DayOfWeek.MONDAY)
        );

        originalLines.clear();
        originalLines.add(3L);

        assertEquals(Set.of(1L, 2L), actual.lines());
    }

    @Test
    void shouldNotChangeWhenOriginalDaysAreModified() {
        var originalDays = EnumSet.of(DayOfWeek.MONDAY);
        var actual = subscriptionWith(
                Set.of(1L, 2L),
                originalDays
        );

        originalDays.clear();
        originalDays.add(DayOfWeek.TUESDAY);

        assertEquals(
                EnumSet.of(DayOfWeek.MONDAY),
                actual.daysOfTheWeek()
        );
    }

    @Test
    void shouldExposeUnmodifiableLines() {
        var actual = subscriptionWith(
                new HashSet<>(Set.of(1L, 2L)),
                EnumSet.of(DayOfWeek.MONDAY)
        );
        var exposedLines = actual.lines();

        assertThrows(
                UnsupportedOperationException.class,
                () -> exposedLines.add(3L)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> exposedLines.remove(1L)
        );
        assertThrows(
                UnsupportedOperationException.class,
                exposedLines::clear
        );

        assertEquals(Set.of(1L, 2L), actual.lines());
    }

    @Test
    void shouldNotChangeWhenReturnedDaysAreModified() {
        var actual = subscriptionWith(
                Set.of(1L, 2L),
                EnumSet.of(DayOfWeek.MONDAY)
        );
        var exposedDays = actual.daysOfTheWeek();

        exposedDays.clear();
        exposedDays.add(DayOfWeek.TUESDAY);

        assertEquals(
                EnumSet.of(DayOfWeek.MONDAY),
                actual.daysOfTheWeek()
        );
    }

    @Test
    void shouldKeepEmptyCollectionsIsolatedFromExternalChanges() {
        var originalLines = new HashSet<Long>();
        var originalDays = EnumSet.noneOf(DayOfWeek.class);
        var actual = subscriptionWith(originalLines, originalDays);

        assertEquals(Set.of(), actual.lines());
        assertEquals(
                EnumSet.noneOf(DayOfWeek.class),
                actual.daysOfTheWeek()
        );

        originalLines.add(1L);
        originalDays.add(DayOfWeek.MONDAY);
        actual.daysOfTheWeek().add(DayOfWeek.TUESDAY);

        assertThrows(
                UnsupportedOperationException.class,
                () -> actual.lines().add(2L)
        );

        assertEquals(Set.of(), actual.lines());
        assertEquals(
                EnumSet.noneOf(DayOfWeek.class),
                actual.daysOfTheWeek()
        );
    }

    private AlertSubscription subscriptionWith(
            Set<Long> lines,
            EnumSet<DayOfWeek> days
    ) {
        return new AlertSubscription(
                lines,
                subscription.startTime(),
                subscription.endTime(),
                days,
                subscription.expiration()
        );
    }
}
