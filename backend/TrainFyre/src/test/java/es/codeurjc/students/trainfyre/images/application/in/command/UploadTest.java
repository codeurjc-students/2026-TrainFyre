package es.codeurjc.students.trainfyre.images.application.in.command;

import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadTest {

    @Test
    void shouldThrowNullPointerExceptionWhenImageIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Upload(
                        new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER)), null
                )
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenAuthenticatedUserIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> new Upload(
                        null,
                        new Image(
                                UUID.randomUUID(),
                                new ByteArrayInputStream(new byte[0]),
                                0,
                                "image/png"
                        )
                )
        );
    }
}
