package es.codeurjc.students.trainfyre.statistics.domain;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidenceDetailsTest {

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
                () -> new IncidenceDetails(null, IncidenceTestBuilder.generateDefaultOccurrence(), IncidenceTestBuilder.generateDefaultDescription(), IncidenceTestBuilder.generateDefaultClassification()),
                () -> new IncidenceDetails(IncidenceTestBuilder.generateDefaultAffectedNetwork(), null, IncidenceTestBuilder.generateDefaultDescription(), IncidenceTestBuilder.generateDefaultClassification()),
                () -> new IncidenceDetails(IncidenceTestBuilder.generateDefaultAffectedNetwork(), IncidenceTestBuilder.generateDefaultOccurrence(), null, IncidenceTestBuilder.generateDefaultClassification()),
                () -> new IncidenceDetails(IncidenceTestBuilder.generateDefaultAffectedNetwork(), IncidenceTestBuilder.generateDefaultOccurrence(), IncidenceTestBuilder.generateDefaultDescription(), null)

        );
    }

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }
}
