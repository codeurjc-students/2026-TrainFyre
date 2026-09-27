package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OccurrenceTest {

    private ZonedDateTime timestamp = ZonedDateTime.now();
    private Duration duration = Duration.ofMinutes(30);

    @Test
    void shouldRejectNullTimestamp(){
        assertThrows(IllegalArgumentException.class, () ->{
           new Occurrence(null, duration);
        });
    }
}
