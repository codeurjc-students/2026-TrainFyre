package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record DataSource(String provider, String externalId) {

    public DataSource {
        Objects.requireNonNull(provider, "provider should not be null");
        Objects.requireNonNull(externalId, "external id should not be null");
    }
}
