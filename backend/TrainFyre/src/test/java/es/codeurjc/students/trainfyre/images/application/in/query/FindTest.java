package es.codeurjc.students.trainfyre.images.application.in.query;

import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FindTest {

    @Test
    void shouldRejectNullUuid() {
        assertThrows(
                NullPointerException.class,
                () -> new Find(null)
        );
    }
}
