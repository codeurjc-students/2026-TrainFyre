package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZonedDateTimeEmbeddable {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, secondPrecision = 6)
    private LocalTime time;

    @Column(nullable = false)
    private String zoneId;

    @Column(nullable = false)
    private int offsetSeconds;

    public ZonedDateTimeEmbeddable(ZonedDateTime value) {
        this.date = value.toLocalDate();
        this.time = value.toLocalTime();
        this.zoneId = value.getZone().getId();
        this.offsetSeconds = value.getOffset().getTotalSeconds();
    }

    public ZonedDateTime toDomain() {
        return ZonedDateTime.ofStrict(
                LocalDateTime.of(date, time),
                ZoneOffset.ofTotalSeconds(offsetSeconds),
                ZoneId.of(zoneId)
        );
    }
}
