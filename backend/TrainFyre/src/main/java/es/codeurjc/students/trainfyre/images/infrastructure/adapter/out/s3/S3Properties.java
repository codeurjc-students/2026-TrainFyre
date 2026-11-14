package es.codeurjc.students.trainfyre.images.infrastructure.adapter.out.s3;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.storage.s3")
public record S3Properties(
        String endpoint,
        @NotBlank String region,
        @NotBlank String bucket,
        String accessKey,
        String secretKey,
        boolean pathStyle
) {
}