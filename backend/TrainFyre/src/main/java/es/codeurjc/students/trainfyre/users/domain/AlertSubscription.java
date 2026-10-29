package es.codeurjc.students.trainfyre.users.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ValueObject
public record AlertSubscription(Set<Long> lines, ZonedDateTime startTime, ZonedDateTime endTime, EnumSet<DayOfWeek> daysOfTheWeek, ZonedDateTime expiration) {

    public AlertSubscription{
        Objects.requireNonNull(lines, "lines should not be null");
        Objects.requireNonNull(startTime, "startTime should not be null");
        Objects.requireNonNull(endTime, "endTime should not be null");
        Objects.requireNonNull(daysOfTheWeek, "daysOfTheWeek should not be null");
        Objects.requireNonNull(expiration, "expiration should not be null");

        lines = Set.copyOf(lines);
        daysOfTheWeek = daysOfTheWeek.clone();
    }

    @Override
    public EnumSet<DayOfWeek> daysOfTheWeek() {
        return daysOfTheWeek.clone();
    }

    public boolean isAffected(List<Long> lineIds, ZonedDateTime timestamp){

        var time = timestamp.toLocalTime();

        return !timestamp.isAfter(expiration)
                && daysOfTheWeek.contains(timestamp.getDayOfWeek())
                && !time.isBefore(startTime.toLocalTime())
                && !time.isAfter(endTime.toLocalTime())
                && lineIds.stream().anyMatch(lines::contains);

    }
}
