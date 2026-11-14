package es.codeurjc.students.trainfyre.images.application.in.command;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Image DEFAULT_IMAGE = new Image(
            USER_ID, new ByteArrayInputStream(new byte[0]), 0, "image/png"
    );
    private static final AuthenticatedUser DEFAULT_USER = new AuthenticatedUser(USER_ID, Set.of(Role.USER));

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
                () -> new Upload(DEFAULT_USER, null),
                () -> new Upload(null, DEFAULT_IMAGE)
        );
    }

    @Test
    void shouldAllowUploadWhenUserIsOwner() {
        Upload upload = new Upload(DEFAULT_USER, DEFAULT_IMAGE);

        assertThatCode(upload::assertCanChangeAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowUploadWhenUserIsAdmin() {
        UUID imageOwnerId = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Image image = new Image(imageOwnerId, new ByteArrayInputStream(new byte[0]), 0, "image/png");
        Upload upload = new Upload(adminUser, image);

        assertThatCode(upload::assertCanChangeAvatar).doesNotThrowAnyException();
    }

    @Test
    void shouldDenyUploadWhenUserIsNotOwnerAndNotAdmin() {
        UUID requesterId = UUID.randomUUID();
        Upload upload = new Upload(
                new AuthenticatedUser(requesterId, Set.of(Role.USER)),
                DEFAULT_IMAGE
        );

        assertThrows(ChangeAvatarDeniedException.class, upload::assertCanChangeAvatar);
    }
}
