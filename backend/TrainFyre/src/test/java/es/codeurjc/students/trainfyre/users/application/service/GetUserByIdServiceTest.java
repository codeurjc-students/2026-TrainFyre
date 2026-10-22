package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import es.codeurjc.students.trainfyre.users.domain.User;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetUserByIdServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("73a06591-345c-4eae-9f49-dfeef592a148");

    private final UserPort userPort = mock(UserPort.class);
    private final GetUserByIdService service = new GetUserByIdService(userPort);

    @Test
    void shouldReturnUserDetailsWhenUserExists() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery(USER_ID);

        AlertSubscription subscription = new AlertSubscription(
                Set.of(1L, 2L),
                ZonedDateTime.parse("2026-10-22T08:00:00Z"),
                ZonedDateTime.parse("2026-10-22T10:00:00Z"),
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
                ZonedDateTime.parse("2026-12-31T23:59:59Z")
        );

        List<AlertSubscription> subscriptions = List.of(subscription);

        User user = User.reconstitute(
                USER_ID,
                "pablo",
                "stored-password",
                subscriptions
        );

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        // When
        UserDetails result = service.execute(query);

        // Then
        assertNotNull(result);
        assertAll(
                () -> assertEquals("pablo", result.username()),
                () -> assertEquals(subscriptions, result.alertSubscriptions())
        );
    }

    @Test
    void shouldReturnEmptySubscriptionsWhenUserHasNoSubscriptions() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery(USER_ID);

        User user = User.reconstitute(
                USER_ID,
                "pablo",
                "stored-password",
                List.of()
        );

        when(userPort.findById(USER_ID, User.class)).thenReturn(user);

        // When
        UserDetails result = service.execute(query);

        // Then
        assertNotNull(result);
        assertAll(
                () -> assertEquals("pablo", result.username()),
                () -> assertEquals(List.of(), result.alertSubscriptions())
        );
    }

    @Test
    void shouldPropagateExceptionWhenFindingUserFails() {
        // Given
        GetUserByIdQuery query = new GetUserByIdQuery(USER_ID);

        RuntimeException persistenceFailure =
                new RuntimeException("Could not find user");

        when(userPort.findById(USER_ID, User.class))
                .thenThrow(persistenceFailure);

        // When
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.execute(query)
        );

        // Then
        assertSame(persistenceFailure, thrown);
    }
}
