package es.codeurjc.students.trainfyre.security.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties("trainfyre.security")
public record SecurityProperties(@Valid @NotNull Jwt jwt, @Valid @NotNull Cors cors) {

    public record Jwt(
            @NotBlank String issuer,
            @NotBlank String audience,
            @NotNull Duration accessTokenTtl,
            @NotBlank String keyId,
            @NotNull Resource privateKey,
            @NotNull Resource publicKey
    ) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }
}
