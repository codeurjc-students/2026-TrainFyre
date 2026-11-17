package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record LineName(String shortName, String longName) {
}
