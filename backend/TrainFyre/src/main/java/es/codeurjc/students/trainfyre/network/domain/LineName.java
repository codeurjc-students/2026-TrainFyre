package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record LineName(String shortName, String longName) {

    public LineName {
        Objects.requireNonNull(shortName, "shortName should not be null");
        Objects.requireNonNull(longName, "longName should not be null");
    }
}
