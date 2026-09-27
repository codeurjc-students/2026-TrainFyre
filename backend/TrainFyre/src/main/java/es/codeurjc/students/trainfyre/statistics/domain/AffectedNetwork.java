package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record AffectedNetwork(long mapId, List<Long> lineIds) {
}
