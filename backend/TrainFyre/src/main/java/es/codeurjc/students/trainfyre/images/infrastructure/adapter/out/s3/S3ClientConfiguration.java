package es.codeurjc.students.trainfyre.images.infrastructure.adapter.out.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(S3Properties.class)
public class S3ClientConfiguration {

    @Bean
    public S3Client s3Client(S3Properties properties) {

        var builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(
                                        properties.pathStyle()
                                )
                                .build()
                )
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);

        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {

            builder.endpointOverride(URI.create(properties.endpoint()));

            if (isBlank(properties.accessKey()) || isBlank(properties.secretKey())) {

                throw new IllegalStateException(
                        "Las credenciales S3 son obligatorias " +
                                "cuando se configura un endpoint personalizado"
                );

            }

            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    properties.accessKey(),
                                    properties.secretKey()
                            )
                    )
            );

        }

        else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
        }

        return builder.build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
