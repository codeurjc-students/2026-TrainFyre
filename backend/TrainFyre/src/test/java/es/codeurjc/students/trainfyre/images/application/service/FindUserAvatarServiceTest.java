package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import es.codeurjc.students.trainfyre.images.application.port.in.query.FindAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserAvatarServiceTest {

    @Mock
    private ImagePort imagePort;

    private FindUserAvatarService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        service = new FindUserAvatarService(imagePort);
    }

    @Test
    void shouldReturnImageWhenUserIsOwner() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Image expectedImage = createTestImage(USER_ID);
        Find query = new Find(authenticatedUser, USER_ID);

        when(imagePort.find(USER_ID)).thenReturn(Optional.of(expectedImage));

        Image result = service.execute(query);

        assertThat(result).isEqualTo(expectedImage);
    }

    @Test
    void shouldReturnImageWhenUserIsAdmin() {
        UUID imageUuid = UUID.randomUUID();
        AuthenticatedUser adminUser = new AuthenticatedUser(USER_ID, Set.of(Role.ADMIN));
        Image expectedImage = createTestImage(imageUuid);
        Find query = new Find(adminUser, imageUuid);

        when(imagePort.find(imageUuid)).thenReturn(Optional.of(expectedImage));

        Image result = service.execute(query);

        assertThat(result).isEqualTo(expectedImage);
    }

    @Test
    void shouldDenyFindWhenUserIsNotOwnerAndNotAdmin() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, Set.of(Role.USER));
        Find query = new Find(authenticatedUser, USER_ID);

        assertThatThrownBy(() -> service.execute(query))
                .isInstanceOf(FindAvatarDeniedException.class)
                .hasMessageContaining("In order to find avatar");
    }

    @Test
    void shouldPropagateExceptionWhenFindingFails() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(USER_ID, Set.of(Role.USER));
        Find query = new Find(authenticatedUser, USER_ID);

        RuntimeException persistenceFailure = new RuntimeException("Could not find image");
        when(imagePort.find(any(UUID.class))).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> service.execute(query))
                .isSameAs(persistenceFailure);
    }

    private Image createTestImage(UUID uuid) {
        String content = "test image content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        return new Image(uuid, inputStream, content.length(), "image/png");
    }
}
