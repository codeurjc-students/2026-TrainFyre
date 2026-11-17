package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record LineColors(HexColor color, HexColor textColor) {

    public LineColors {
        Objects.requireNonNull(color, "color should not be null");
        Objects.requireNonNull(textColor, "textColor should not be null");
    }

}
