package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(Duration.ZERO, occurrence.duration());
    }

    @Test
    void durationShouldNotBeNegative(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Occurrence(timestamp, Duration.ofMinutes(-2));
        });
    }

    @Test
    void shouldReturnInstantaneusIfDurationIsZero(){
        Occurrence occurrence = new Occurrence(timestamp, Duration.ZERO);
        assertEquals(true, occurrence.isInstantaneous());
    }

    @Test
    void shouldReturnEndTime(){
        Occurrence occurrence = new Occurrence(timestamp, duration);
        assertEquals(timestamp.plus(duration), occurrence.endsAt());
    }
}
