package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record LineId(Long value) {

    public LineId {
        Objects.requireNonNull(value, "id should not be null");
    }
}
