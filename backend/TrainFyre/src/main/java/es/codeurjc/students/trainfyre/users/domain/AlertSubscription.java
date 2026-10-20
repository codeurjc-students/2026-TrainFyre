package es.codeurjc.students.trainfyre.users.domain;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;

public record AlertSubscription(Set<Long> lines, ZonedDateTime startTime, ZonedDateTime endTime, EnumSet<DayOfWeek> daysOfTheWeek, ZonedDateTime expiration) {
}
