package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record IncidenceDetails(AffectedNetwork affectedNetwork, Occurrence occurrence, Description description, Classification classification) {

    public IncidenceDetails{
        Objects.requireNonNull(affectedNetwork, "affected network should not be null");
        Objects.requireNonNull(occurrence, "occurrence should not be null");
        Objects.requireNonNull(description, "description should not be null");
        Objects.requireNonNull(classification, "classification should not be null");
    }
}
