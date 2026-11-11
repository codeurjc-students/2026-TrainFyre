package es.codeurjc.students.trainfyre.images.application.in.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadTest {

    @Test
    void shouldThrowNullPointerExceptionWhenImageIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Upload(null)
        );
    }
}
