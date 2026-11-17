package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record DataSource(String provider, String externalId) {
}
