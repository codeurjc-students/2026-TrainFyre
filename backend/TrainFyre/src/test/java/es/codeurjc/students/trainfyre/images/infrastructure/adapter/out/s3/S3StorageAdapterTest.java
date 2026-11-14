package es.codeurjc.students.trainfyre.images.infrastructure.adapter.out.s3;

import es.codeurjc.students.trainfyre.images.domain.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    private S3Properties s3Properties;
    private S3StorageAdapter s3StorageAdapter;

    private static final String BUCKET_NAME = "test-bucket";
    private static final UUID IMAGE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String CONTENT_TYPE = "image/png";
    private static final byte[] IMAGE_CONTENT = "image content".getBytes();

    @BeforeEach
    void setUp() {
        s3Properties = new S3Properties("http://localhost:9000", "eu-west-1", BUCKET_NAME, "accessKey", "secretKey", true);
        s3StorageAdapter = new S3StorageAdapter(s3Client, s3Properties);
    }

    @Test
    void shouldSaveImageInS3() {
        Image image = new Image(IMAGE_UUID, new ByteArrayInputStream(IMAGE_CONTENT), IMAGE_CONTENT.length, CONTENT_TYPE);

        s3StorageAdapter.save(image);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo(IMAGE_UUID.toString());
        assertThat(request.contentType()).isEqualTo(CONTENT_TYPE);
    }

    @Test
    void shouldThrowObjectStorageExceptionWhenSaveFails() {
        Image image = new Image(IMAGE_UUID, new ByteArrayInputStream(IMAGE_CONTENT), IMAGE_CONTENT.length, CONTENT_TYPE);
        SdkException sdkException = SdkException.builder().message("S3 error").build();
        doThrow(sdkException).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThatThrownBy(() -> s3StorageAdapter.save(image))
                .isInstanceOf(ObjectStorageException.class)
                .hasMessageContaining("Image could not be saved")
                .hasCause(sdkException);
    }

    @Test
    void shouldFindImageInS3() {
        GetObjectResponse getObjectResponse = GetObjectResponse.builder()
                .contentType(CONTENT_TYPE)
                .contentLength((long) IMAGE_CONTENT.length)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
        when(responseInputStream.response()).thenReturn(getObjectResponse);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        Optional<Image> foundImage = s3StorageAdapter.find(IMAGE_UUID);

        assertThat(foundImage).isPresent();
        Image image = foundImage.get();
        assertThat(image.uuid()).isEqualTo(IMAGE_UUID);
        assertThat(image.contentType()).isEqualTo(CONTENT_TYPE);
        assertThat(image.contentLength()).isEqualTo(IMAGE_CONTENT.length);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(requestCaptor.capture());
        GetObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo(IMAGE_UUID.toString());
    }

    @Test
    void shouldReturnEmptyWhenImageNotFound() {
        S3Exception s3Exception = mock(S3Exception.class);
        when(s3Exception.statusCode()).thenReturn(404);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(s3Exception);

        Optional<Image> foundImage = s3StorageAdapter.find(IMAGE_UUID);

        assertThat(foundImage).isEmpty();
    }

    @Test
    void shouldThrowObjectStorageExceptionWhenFindFailsWithS3Exception() {
        S3Exception s3Exception = mock(S3Exception.class);
        when(s3Exception.statusCode()).thenReturn(500);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(s3Exception);

        assertThatThrownBy(() -> s3StorageAdapter.find(IMAGE_UUID))
                .isInstanceOf(ObjectStorageException.class)
                .hasMessageContaining("Could not retrieve the picture")
                .hasCause(s3Exception);
    }

    @Test
    void shouldThrowObjectStorageExceptionWhenFindFailsWithSdkException() {
        SdkException sdkException = SdkException.builder().message("SDK error").build();
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(sdkException);

        assertThatThrownBy(() -> s3StorageAdapter.find(IMAGE_UUID))
                .isInstanceOf(ObjectStorageException.class)
                .hasMessageContaining("Image could not be saved")
                .hasCause(sdkException);
    }

    @Test
    void shouldUseDefaultContentTypeWhenMetadataIsNull() {
        GetObjectResponse getObjectResponse = GetObjectResponse.builder()
                .contentType(null)
                .contentLength((long) IMAGE_CONTENT.length)
                .build();

        ResponseInputStream<GetObjectResponse> responseInputStream = mock(ResponseInputStream.class);
        when(responseInputStream.response()).thenReturn(getObjectResponse);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        Optional<Image> foundImage = s3StorageAdapter.find(IMAGE_UUID);

        assertThat(foundImage).isPresent();
        Image image = foundImage.get();
        assertThat(image.contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldDeleteImageFromS3() {
        s3StorageAdapter.delete(IMAGE_UUID);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo(BUCKET_NAME);
        assertThat(request.key()).isEqualTo(IMAGE_UUID.toString());
    }

    @Test
    void shouldThrowObjectStorageExceptionWhenDeleteFails() {
        SdkException sdkException = SdkException.builder().message("S3 error").build();
        doThrow(sdkException).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> s3StorageAdapter.delete(IMAGE_UUID))
                .isInstanceOf(ObjectStorageException.class)
                .hasMessageContaining("Image could not be saved")
                .hasCause(sdkException);
    }
}
