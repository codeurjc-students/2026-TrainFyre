package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;
import java.util.UUID;

@ValueObject
public record MapId(UUID value) {

    public MapId {
        Objects.requireNonNull(value, "map id should not be null");
    }

    public static MapId generate() {
        return null;
    }
}
