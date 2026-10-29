package es.codeurjc.students.trainfyre.users.application.port.in.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteUserCommandTest {

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new DeleteUserCommand(null)
        );
    }
}
