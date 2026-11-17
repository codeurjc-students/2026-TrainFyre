package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record LineColors(HexColor color, HexColor textColor) {
}
