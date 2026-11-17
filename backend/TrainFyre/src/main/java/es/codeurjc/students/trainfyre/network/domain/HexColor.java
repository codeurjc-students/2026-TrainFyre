package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record HexColor(String value) {

    public HexColor {
        Objects.requireNonNull(value, "color should not be null");
    }
}
