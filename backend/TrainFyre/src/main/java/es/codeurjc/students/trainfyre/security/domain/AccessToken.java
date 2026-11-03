package es.codeurjc.students.trainfyre.security.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Duration;
import java.util.Objects;

@ValueObject
public record AccessToken(String value, Duration lifetime) {

    public AccessToken {
        Objects.requireNonNull(value, "value should not be null");
        Objects.requireNonNull(lifetime, "lifetime should not be null");
        if (value.isBlank()) throw new IllegalArgumentException("value should not be blank");
        if (lifetime.isZero() || lifetime.isNegative()) throw new IllegalArgumentException("lifetime should be positive");
    }

    @Override
    public String toString() {
        return "AccessToken[value=****, lifetime=" + lifetime + "]";
    }
}
