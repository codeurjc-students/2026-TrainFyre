package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static es.codeurjc.students.trainfyre.statistics.domain.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
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

        assertThrows(IllegalArgumentException.class, () -> anIncidence().withClassification(informalClassification).withOccurrence(notZeroDurationOccurrence).build());

    }

    static Stream<Arguments> validIncidenceChanges() {

        Description newDescription = new Description("New Description", "This is the new description!!!");

        AffectedNetwork newAffectedNetwork = new AffectedNetwork(3L, List.of(1L));

        Occurrence newOccurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(50));

        Classification newClassification = new Classification(Severity.CRITICAL, Cause.TECHNICAL_PROBLEM);

        return Stream.of(
                Arguments.of(
                        "description",
                        (Consumer<Incidence>)
                                incidence ->
                                        incidence.changeDescription(
                                                newDescription
                                        ),
                        (Function<Incidence, Object>)
                                Incidence::getDescription,
                        newDescription
                ),
                Arguments.of(
                        "affected network",
                        (Consumer<Incidence>)
                                incidence ->
                                        incidence.changeAffectedNetwork(
                                                newAffectedNetwork
                                        ),
                        (Function<Incidence, Object>)
                                Incidence::getAffectedNetwork,
                        newAffectedNetwork
                ),
                Arguments.of(
                        "occurrence",
                        (Consumer<Incidence>)
                                incidence ->
                                        incidence.changeOccurrence(
                                                newOccurrence
                                        ),
                        (Function<Incidence, Object>)
                                Incidence::getOccurrence,
                        newOccurrence
                ),
                Arguments.of(
                        "classification",
                        (Consumer<Incidence>)
                                incidence ->
                                        incidence.changeClassification(
                                                newClassification
                                        ),
                        (Function<Incidence, Object>)
                                Incidence::getClassification,
                        newClassification
                )
        );
    }

    @ParameterizedTest(name = "should allow changing {0}")
    @MethodSource("validIncidenceChanges")
    void shouldAllowIncidenceChanges(String field, Consumer<Incidence> change, Function<Incidence, Object> getter, Object expectedValue)
    {
        Incidence incidence = anIncidence().build();

        change.accept(incidence);

        assertEquals(expectedValue, getter.apply(incidence));
    }

    static Stream<Arguments> invalidNullChanges() {
        return Stream.of(
                Arguments.of("affected network",(Executable) () -> anIncidence().build().changeAffectedNetwork(null)),
                Arguments.of("occurrence",(Executable) () -> anIncidence().build().changeOccurrence(null)),
                Arguments.of("description",(Executable) () -> anIncidence().build().changeDescription(null)),
                Arguments.of("classification",(Executable) () -> anIncidence().build().changeClassification(null))
        );
    }

    @ParameterizedTest(name = "should reject null {0} when changed")
    @MethodSource("invalidNullChanges")
    void shouldRejectNullArgumentsWhenChanged(String field, Executable change) {
        assertThrows(IllegalArgumentException.class, change);
    }

    @Test
    void shouldNotAllowAnInformationalIncidenceToChangeToNonZeroDuration(){

        Classification informalClassification = new Classification(Severity.INFORMATIONAL, Cause.MEDICAL_EMERGENCY);
        Occurrence zeroDurationOccurrence = new Occurrence(ZonedDateTime.now(), Duration.ZERO);
        Occurrence notZeroDurationOccurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(30));

        Incidence incidence = anIncidence().withClassification(informalClassification).withOccurrence(zeroDurationOccurrence).build();

        assertThrows(IllegalArgumentException.class, () -> incidence.changeOccurrence(notZeroDurationOccurrence));

    }

    @Test
    void shouldNotAllowAnNonZeroDurationIncidenceToChangeToInformational(){

        Classification criticalClassification = new Classification(Severity.CRITICAL, Cause.MEDICAL_EMERGENCY);
        Classification informalClassification = new Classification(Severity.INFORMATIONAL, Cause.MEDICAL_EMERGENCY);
        Occurrence notZeroDurationOccurrence = new Occurrence(ZonedDateTime.now(), Duration.ofMinutes(30));

        Incidence incidence = anIncidence().withClassification(criticalClassification).withOccurrence(notZeroDurationOccurrence).build();

        assertThrows(IllegalArgumentException.class, () -> incidence.changeClassification(informalClassification));

    }

}
