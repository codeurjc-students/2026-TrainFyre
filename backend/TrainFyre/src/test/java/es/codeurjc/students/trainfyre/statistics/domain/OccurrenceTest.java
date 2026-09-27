package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.ZonedDateTime;



class OccurrenceTest {

    private ZonedDateTime timestamp = ZonedDateTime.now();
    private Duration duration = Duration.ofMinutes(30);

    @Test
    void shouldRejectNullTimestamp(){
        assertThrows(IllegalArgumentException.class, () ->{
           new Occurrence(null, duration);
        });
    }

    @Test
    void durationShouldBeZeroIfNull(){
        Occurrence occurrence = new Occurrence(timestamp, null);
        Assertions.assertEquals(Duration.ZERO, occurrence.duration());
    }
}
