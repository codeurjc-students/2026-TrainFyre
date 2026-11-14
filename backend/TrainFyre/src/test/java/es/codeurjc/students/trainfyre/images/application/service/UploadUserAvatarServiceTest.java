package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadUserAvatarServiceTest {

    @Mock
    private ImagePort imagePort;

    private UploadUserAvatarService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Image DEFAULT_IMAGE = new Image(
            USER_ID, new ByteArrayInputStream("image content".getBytes()), 14, "image/png"
    );

    @BeforeEach
    void setUp() {
        service = new UploadUserAvatarService(imagePort);
    }

    @Test
    void shouldSaveImageWhenUserIsOwner() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Upload command = new Upload(authenticatedUser, DEFAULT_IMAGE);

        service.execute(command);

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(DEFAULT_IMAGE);
    }

    @Test
    void shouldSaveImageWhenUserIsAdmin() {
        UUID imageUuid = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Image image = new Image(imageUuid, new ByteArrayInputStream("image content".getBytes()), 14, "image/png");
        Upload command = new Upload(adminUser, image);

        service.execute(command);

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(image);
    }

    @Test
    void shouldDenyUploadWhenUserIsNotOwnerAndNotAdmin() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Upload command = new Upload(authenticatedUser, DEFAULT_IMAGE);

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ChangeAvatarDeniedException.class)
                .hasMessageContaining("In order to change avatar");
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Upload command = new Upload(authenticatedUser, DEFAULT_IMAGE);

        RuntimeException persistenceFailure = new RuntimeException("Could not save image");
        doThrow(persistenceFailure).when(imagePort).save(any(Image.class));

        assertThatThrownBy(() -> service.execute(command))
                .isSameAs(persistenceFailure);
    }
}
