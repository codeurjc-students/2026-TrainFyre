package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Duration;
import java.time.ZonedDateTime;

@ValueObject
public record Occurrence(ZonedDateTime timestamp, Duration duration) {

    public Occurrence{
        if(timestamp == null) throw new IllegalArgumentException("timestamp should not be null");
        if(duration == null) duration = Duration.ZERO;
    }
}
