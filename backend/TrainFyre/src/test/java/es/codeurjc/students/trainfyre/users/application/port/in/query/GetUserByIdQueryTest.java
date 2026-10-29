package es.codeurjc.students.trainfyre.users.application.port.in.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GetUserByIdQueryTest {

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> new GetUserByIdQuery(null)
        );
    }
}
