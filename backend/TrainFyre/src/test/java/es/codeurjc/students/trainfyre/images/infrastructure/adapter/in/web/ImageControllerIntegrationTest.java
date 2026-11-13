package es.codeurjc.students.trainfyre.images.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.application.service.UploadUserAvatarService;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.in.jwt.AuthenticatedUserJwtConverter;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.out.jwt.NimbusAccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.infrastructure.config.JwtConfiguration;
import es.codeurjc.students.trainfyre.security.infrastructure.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ImageController.class)
@Import({
        UploadUserAvatarService.class,
        SecurityConfiguration.class,
        JwtConfiguration.class,
        NimbusAccessTokenIssuer.class,
        AuthenticatedUserJwtConverter.class
})
class ImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @MockitoBean
    private ImagePort imagePort;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final byte[] IMAGE_CONTENT = "image content".getBytes();
    private static final String IMAGE_CONTENT_TYPE = "image/png";

    private String bearer(UUID userId) {
        var user = new AuthenticatedUser(userId, Set.of(Role.USER));
        return "Bearer " + accessTokenIssuer.issue(user).value();
    }

    private String bearerAdmin(UUID userId) {
        var user = new AuthenticatedUser(userId, Set.of(Role.ADMIN));
        return "Bearer " + accessTokenIssuer.issue(user).value();
    }

    @Test
    void shouldUploadAvatarAndReturnNoContentWhenUserIsOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());

        Image savedImage = captor.getValue();
        assertThat(savedImage.uuid()).isEqualTo(USER_ID);
        assertThat(savedImage.contentType()).isEqualTo(IMAGE_CONTENT_TYPE);
        assertThat(savedImage.contentLength()).isEqualTo(IMAGE_CONTENT.length);
    }

    @Test
    void shouldUploadAvatarAndReturnNoContentWhenUserIsAdmin() throws Exception {
        UUID imageOwnerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", imageOwnerId)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());

        Image savedImage = captor.getValue();
        assertThat(savedImage.uuid()).isEqualTo(imageOwnerId);
    }

    @Test
    void shouldDenyUploadWhenUserIsNotOwnerAndNotAdmin() throws Exception {
        UUID imageOwnerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", imageOwnerId)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerId)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectRequestWithMalformedToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                IMAGE_CONTENT_TYPE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(emptyFile)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectMissingFileParameter() throws Exception {
        mockMvc.perform(put("/api/v1/images/{id}/avatar", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imagePort);
    }

    @ParameterizedTest(name = "[{index}] contentType={0}")
    @ValueSource(strings = {"image/jpeg", "image/gif", "image/webp"})
    void shouldUploadAvatarWithDifferentImageFormats(String contentType) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar." + contentType.split("/")[1],
                contentType,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());
        assertThat(captor.getValue().contentType()).isEqualTo(contentType);
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() throws Exception {
        doThrow(new RuntimeException("Storage failure")).when(imagePort).save(any(Image.class));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldPropagateChangeAvatarDeniedExceptionWithForbiddenStatus() throws Exception {
        UUID imageOwnerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", imageOwnerId)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerId)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldUploadLargeFile() throws Exception {
        byte[] largeContent = new byte[1024 * 1024]; // 1 MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large_avatar.png",
                IMAGE_CONTENT_TYPE,
                largeContent
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());
        assertThat(captor.getValue().contentLength()).isEqualTo(largeContent.length);
    }

    @Test
    void shouldUploadFileWithOriginalFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my-custom-avatar.jpg",
                "image/jpeg",
                IMAGE_CONTENT
        );

        mockMvc.perform(multipart("/api/v1/images/{id}/avatar", USER_ID)
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        verify(imagePort).save(any(Image.class));
    }
}
