package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Locale;
import java.util.Objects;

/**
 * Natural key of a {@link TransportMap} in the outside world: who publishes it ({@code provider},
 * e.g. {@code "renfe"}) and how that publisher identifies it ({@code externalId}).
 * The provider is case-insensitive, the external id keeps its case.
 */
@ValueObject
public record DataSource(String provider, String externalId) {

    public DataSource {
        provider = requireText(provider, "provider").toLowerCase(Locale.ROOT);
        externalId = requireText(externalId, "external id");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " should not be null");

        String stripped = value.strip();
        if (stripped.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " should not be empty or blank");
        }
        return stripped;
    }
}
