package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static es.codeurjc.students.trainfyre.statistics.domain.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class IncidenceTest {

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
                () -> anIncidence().withAffectedNetwork(null).build(),
                () -> anIncidence().withOccurrence(null).build(),
                () -> anIncidence().withDescription(null).build(),
                () -> anIncidence().withClassification(null).build()
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(IllegalArgumentException.class, creation);
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
