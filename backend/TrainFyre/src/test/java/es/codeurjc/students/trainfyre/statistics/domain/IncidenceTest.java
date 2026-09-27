package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidenceTest {

    private AffectedNetwork affectedNetwork = new AffectedNetwork(1L, Arrays.asList(1L, 2L, 3L));
    private Occurrence occurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(30));
    private Description description = new Description("Train failure", "The train's engine has broken down and won't start.");
    private Classification classification = new Classification(Severity.MODERATE, Cause.MEDICAL_EMERGENCY);

    @Test
    void shouldRejectNullAffectedNetwork(){
        assertThrows(IllegalArgumentException.class, () -> {
            Incidence.createIncidence(null, occurrence, description, classification);
        });
    }
    @Test
    void shouldRejectNullOccurrence(){
        assertThrows(IllegalArgumentException.class, () -> {
            Incidence.createIncidence(affectedNetwork, null, description, classification);
        });
    }
    @Test
    void shouldRejectNullDescription(){
        assertThrows(IllegalArgumentException.class, () -> {
            Incidence.createIncidence(affectedNetwork, occurrence, null, classification);
        });
    }
    @Test
    void shouldRejectNullClassification(){
        assertThrows(IllegalArgumentException.class, () -> {
            Incidence.createIncidence(affectedNetwork, occurrence, description, null);
        });
    }


}
