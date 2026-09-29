package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static es.codeurjc.students.trainfyre.statistics.domain.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidenceTest {

    @Test
    void shouldRejectNullAffectedNetwork(){
        assertThrows(IllegalArgumentException.class, () -> {
            anIncidence().withAffectedNetwork(null).build();
        });
    }
    @Test
    void shouldRejectNullOccurrence(){
        assertThrows(IllegalArgumentException.class, () -> {
            anIncidence().withOccurrence(null).build();
        });
    }
    @Test
    void shouldRejectNullDescription(){
        assertThrows(IllegalArgumentException.class, () -> {
            anIncidence().withDescription(null).build();
        });
    }
    @Test
    void shouldRejectNullClassification(){
        assertThrows(IllegalArgumentException.class, () -> {
            anIncidence().withClassification(null).build();
        });
    }
    @Test
    void shouldRejectInformationalIncidenceWithNonZeroDuration(){

        Classification informalClassification = new Classification(Severity.INFORMATIONAL, Cause.MEDICAL_EMERGENCY);
        Occurrence notZeroDurationOccurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> {
            anIncidence().withClassification(informalClassification).withOccurrence(notZeroDurationOccurrence).build();
        });

    }
    @Test
    void shouldAllowToChangeDescription(){

        Description newDescription = new Description("New Description", "This is the new description!!!");
        Incidence incidence = anIncidence().build();

        incidence.changeDescription(newDescription);
        assertEquals(newDescription, incidence.getDescription());

    }



}
