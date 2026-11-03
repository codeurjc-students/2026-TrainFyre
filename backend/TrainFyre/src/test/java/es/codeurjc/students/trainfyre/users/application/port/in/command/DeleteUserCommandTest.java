package es.codeurjc.students.trainfyre.users.application.port.in.command;

import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteUserCommandTest {

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new DeleteUserCommand(
                        new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER)), null
                )
        );
    }
}
