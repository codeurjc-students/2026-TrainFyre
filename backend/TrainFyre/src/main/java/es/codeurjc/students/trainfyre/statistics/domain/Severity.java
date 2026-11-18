package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum Severity {
    CRITICAL, HIGH, MODERATE, LOW,  INFORMATIONAL
}
