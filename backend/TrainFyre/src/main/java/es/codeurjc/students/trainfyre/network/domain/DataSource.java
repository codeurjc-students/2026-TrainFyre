package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record DataSource(String provider, String externalId) {

    public DataSource {
        Objects.requireNonNull(provider, "provider should not be null");
        Objects.requireNonNull(externalId, "external id should not be null");
        if(provider.isBlank()) throw new IllegalArgumentException("provider should not be empty or blank");
        if(externalId.isBlank()) throw new IllegalArgumentException("provider should not be empty or blank");
        provider = provider.strip().toLowerCase();
        externalId = externalId.strip();
    }
}
