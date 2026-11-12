package es.codeurjc.students.trainfyre.images.infrastructure.adapter.out.s3;

import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.domain.Image;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class S3StorageAdapter implements ImagePort {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public void save(Image image) {

        try {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(image.uuid().toString())
                    .contentType(image.contentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(image.content(), image.contentLength()));

        }
        catch (SdkException e) {
            throw new ObjectStorageException("Image could not be saved", e);
        }

    }

    @Override
    public Optional<Image> find(UUID uuid) {

        try {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(uuid.toString())
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

            GetObjectResponse metadata = response.response();

            String contentType = metadata.contentType() != null ? metadata.contentType() : "application/octet-stream";

            long contentLength = metadata.contentLength() != null ? metadata.contentLength() : 0L;

            return Optional.of(
                    new Image(
                            uuid,
                            response,
                            contentLength,
                            contentType
                    )
            );

        } catch (S3Exception e) {

            if (e.statusCode() == 404) {
                return Optional.empty();
            }

            throw new ObjectStorageException("No se pudo recuperar la fotografía", e);

        } catch (SdkException e) {
            throw new ObjectStorageException("Image could not be saved", e);
        }

    }

    @Override
    public void delete(UUID uuid) {

        try {

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(uuid.toString())
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (SdkException e) {
            throw new ObjectStorageException("Image could not be saved", e);
        }


    }
}
