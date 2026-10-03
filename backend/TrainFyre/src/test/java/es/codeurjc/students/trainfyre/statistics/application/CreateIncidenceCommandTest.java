package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateIncidenceCommandTest {

    static Stream<Executable> invalidNullArguments(){
        return Stream.of(
                () -> new CreateIncidenceCommand(null, IncidenceTestBuilder.generateDefaultOccurrence(), IncidenceTestBuilder.generateDefaultDescription(), IncidenceTestBuilder.generateDefaultClassification()),
                () -> new CreateIncidenceCommand(IncidenceTestBuilder.generateDefaultAffectedNetwork(), null, IncidenceTestBuilder.generateDefaultDescription(), IncidenceTestBuilder.generateDefaultClassification()),
                () -> new CreateIncidenceCommand(IncidenceTestBuilder.generateDefaultAffectedNetwork(), IncidenceTestBuilder.generateDefaultOccurrence(), null, IncidenceTestBuilder.generateDefaultClassification()),
                () -> new CreateIncidenceCommand(IncidenceTestBuilder.generateDefaultAffectedNetwork(), IncidenceTestBuilder.generateDefaultOccurrence(), IncidenceTestBuilder.generateDefaultDescription(), null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }


}
