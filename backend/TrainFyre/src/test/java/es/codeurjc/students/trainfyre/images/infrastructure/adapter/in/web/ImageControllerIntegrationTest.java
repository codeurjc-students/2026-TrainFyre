package es.codeurjc.students.trainfyre.images.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.images.application.port.in.command.ChangeAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.in.query.FindAvatarDeniedException;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.application.service.DeleteUserAvatarService;
import es.codeurjc.students.trainfyre.images.application.service.FindUserAvatarService;
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
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ImageController.class)
@Import({
        DeleteUserAvatarService.class,
        FindUserAvatarService.class,
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

    private MockMultipartHttpServletRequestBuilder multipartPut(UUID imageId, MockMultipartFile file) {
        return multipart("/api/v1/avatars/{id}", imageId)
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
    }

    private MockMultipartHttpServletRequestBuilder multipartPutMe(MockMultipartFile file) {
        return multipart("/api/v1/avatars/me")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
    }

    @Test
    void shouldUploadAvatarAndReturnNoContentWhenUserIsOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipartPut(USER_ID, file)
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

        mockMvc.perform(multipartPut(imageOwnerId, file)
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());

        Image savedImage = captor.getValue();
        assertThat(savedImage.uuid()).isEqualTo(imageOwnerId);
    }

    @Test
    void shouldUploadOwnAvatarAndReturnNoContentWhenUsingMeEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipartPutMe(file)
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
    void shouldRejectMeEndpointRequestWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipartPutMe(file))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectMeEndpointRequestWithMalformedToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        mockMvc.perform(multipartPutMe(file)
                        .header(HttpHeaders.AUTHORIZATION, "******"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateEmptyFileValidationErrorForMeEndpoint() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                IMAGE_CONTENT_TYPE,
                new byte[0]
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPutMe(emptyFile)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(new IllegalArgumentException("image file cannot be empty"));

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectMissingFileParameterForMeEndpoint() throws Exception {
        mockMvc.perform(put("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateExceptionWhenSavingMeAvatarFails() {
        doThrow(new RuntimeException("Storage failure")).when(imagePort).save(any(Image.class));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPutMe(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(new RuntimeException("Storage failure"));
    }

    @Test
    void shouldPropagateChangeAvatarDeniedExceptionWhenUserIsNotOwnerAndNotAdmin() {
        UUID imageOwnerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPut(imageOwnerId, file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerId))))
                .hasCauseInstanceOf(ChangeAvatarDeniedException.class);

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

        mockMvc.perform(multipartPut(USER_ID, file))
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

        mockMvc.perform(multipartPut(USER_ID, file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateEmptyFileValidationError() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                IMAGE_CONTENT_TYPE,
                new byte[0]
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPut(USER_ID, emptyFile)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                        .hasCause(new IllegalArgumentException("image file cannot be empty"));

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectMissingFileParameter() throws Exception {
        mockMvc.perform(put("/api/v1/avatars/{id}", USER_ID)
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

        mockMvc.perform(multipartPut(USER_ID, file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
        verify(imagePort).save(captor.capture());
        assertThat(captor.getValue().contentType()).isEqualTo(contentType);
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        doThrow(new RuntimeException("Storage failure")).when(imagePort).save(any(Image.class));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPut(USER_ID, file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                        .hasCause(new RuntimeException("Storage failure"));
    }

    @Test
    void shouldPropagateChangeAvatarDeniedException() {
        UUID imageOwnerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                IMAGE_CONTENT_TYPE,
                IMAGE_CONTENT
        );

        assertThatThrownBy(() -> mockMvc.perform(multipartPut(imageOwnerId, file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attackerId))))
                .hasCauseInstanceOf(ChangeAvatarDeniedException.class);

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

        mockMvc.perform(multipartPut(USER_ID, file)
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

        mockMvc.perform(multipartPut(USER_ID, file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        verify(imagePort).save(any(Image.class));
    }

    @Test
    void shouldFindAvatarAndReturnItWhenUserIsOwner() throws Exception {
        Image image = image(USER_ID);
        when(imagePort.find(USER_ID)).thenReturn(Optional.of(image));

        mockMvc.perform(get("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(USER_ID.toString()))
                .andExpect(jsonPath("$.contentType").value(IMAGE_CONTENT_TYPE))
                .andExpect(jsonPath("$.contentLength").value(IMAGE_CONTENT.length));

        verify(imagePort).find(USER_ID);
    }

    @Test
    void shouldFindAvatarAndReturnItWhenUserIsAdmin() throws Exception {
        UUID imageOwnerId = UUID.randomUUID();
        when(imagePort.find(imageOwnerId)).thenReturn(Optional.of(image(imageOwnerId)));

        mockMvc.perform(get("/api/v1/avatars/{id}", imageOwnerId)
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(imageOwnerId.toString()))
                .andExpect(jsonPath("$.contentType").value(IMAGE_CONTENT_TYPE));

        verify(imagePort).find(imageOwnerId);
    }

    @Test
    void shouldFindOwnAvatarAndReturnItWhenUsingMeEndpoint() throws Exception {
        when(imagePort.find(USER_ID)).thenReturn(Optional.of(image(USER_ID)));

        mockMvc.perform(get("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(USER_ID.toString()))
                .andExpect(jsonPath("$.contentType").value(IMAGE_CONTENT_TYPE))
                .andExpect(jsonPath("$.contentLength").value(IMAGE_CONTENT.length));

        verify(imagePort).find(USER_ID);
    }

    @Test
    void shouldRejectFindOwnAvatarRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/avatars/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectFindOwnAvatarRequestWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, "******"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateExceptionWhenOwnAvatarIsNotFound() {
        when(imagePort.find(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCauseInstanceOf(java.util.NoSuchElementException.class);

        verify(imagePort).find(USER_ID);
    }

    @Test
    void shouldPropagateExceptionWhenFindingOwnAvatarFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not find image");
        when(imagePort.find(USER_ID)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(persistenceFailure);

        verify(imagePort).find(USER_ID);
    }

    @Test
    void shouldPropagateFindAvatarDeniedExceptionWhenUserIsNotOwnerAndNotAdmin() {
        UUID imageOwnerId = UUID.randomUUID();

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/avatars/{id}", imageOwnerId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCauseInstanceOf(FindAvatarDeniedException.class);

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectFindAvatarRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/avatars/{id}", USER_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectFindAvatarRequestWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, "******"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectFindAvatarRequestWithMalformedImageId() throws Exception {
        mockMvc.perform(get("/api/v1/avatars/not-a-uuid")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateExceptionWhenAvatarIsNotFound() {
        when(imagePort.find(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCauseInstanceOf(java.util.NoSuchElementException.class);

        verify(imagePort).find(USER_ID);
    }

    @Test
    void shouldPropagateExceptionWhenFindingAvatarFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not find image");
        when(imagePort.find(USER_ID)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> mockMvc.perform(get("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(persistenceFailure);
    }

    @Test
    void shouldDeleteAvatarAndReturnNoContentWhenUserIsOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(imagePort).delete(USER_ID);
    }

    @Test
    void shouldDeleteAvatarAndReturnNoContentWhenUserIsAdmin() throws Exception {
        UUID imageOwnerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/avatars/{id}", imageOwnerId)
                        .header(HttpHeaders.AUTHORIZATION, bearerAdmin(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(imagePort).delete(imageOwnerId);
    }

    @Test
    void shouldPropagateChangeAvatarDeniedExceptionWhenDeletingAvatarAsUnauthorizedUser() {
        UUID imageOwnerId = UUID.randomUUID();

        assertThatThrownBy(() -> mockMvc.perform(delete("/api/v1/avatars/{id}", imageOwnerId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCauseInstanceOf(ChangeAvatarDeniedException.class);

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectDeleteAvatarRequestWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/{id}", USER_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectDeleteAvatarRequestWithMalformedToken() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, "******"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectDeleteAvatarRequestWithMalformedImageId() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/not-a-uuid")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingAvatarFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not delete image");
        doThrow(persistenceFailure).when(imagePort).delete(USER_ID);

        assertThatThrownBy(() -> mockMvc.perform(delete("/api/v1/avatars/{id}", USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(persistenceFailure);

        verify(imagePort).delete(USER_ID);
    }

    @Test
    void shouldDeleteOwnAvatarAndReturnNoContentWhenUsingMeEndpoint() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(imagePort).delete(USER_ID);
    }

    @Test
    void shouldRejectDeleteOwnAvatarRequestWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldRejectDeleteOwnAvatarRequestWithMalformedToken() throws Exception {
        mockMvc.perform(delete("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, "******"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imagePort);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingOwnAvatarFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not delete image");
        doThrow(persistenceFailure).when(imagePort).delete(USER_ID);

        assertThatThrownBy(() -> mockMvc.perform(delete("/api/v1/avatars/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))))
                .hasCause(persistenceFailure);

        verify(imagePort).delete(USER_ID);
    }

    private Image image(UUID uuid) {
        return new Image(
                uuid,
                new ByteArrayInputStream(IMAGE_CONTENT),
                IMAGE_CONTENT.length,
                IMAGE_CONTENT_TYPE
        );
    }
}
