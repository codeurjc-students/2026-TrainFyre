package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Classification (Severity severity, Cause cause) {
}