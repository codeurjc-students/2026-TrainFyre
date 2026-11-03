package es.codeurjc.students.trainfyre.users.application.port.in.query;

import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GetUserByIdQueryTest {

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> new GetUserByIdQuery(
                        new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER)), null
                )
        );
    }
}
