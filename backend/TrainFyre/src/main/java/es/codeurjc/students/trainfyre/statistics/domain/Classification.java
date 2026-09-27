package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Classification (Severity severity, Cause cause) {

    public Classification {
        if(severity == null) throw new IllegalArgumentException("severity should not be null");
        if(cause == null) throw new IllegalArgumentException("cause should not be null");
    }
}