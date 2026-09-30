package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateIncidenceCommandTest {

    AffectedNetwork affectedNetwork = IncidenceTestBuilder.generateDefaultAffectedNetwork();
    Occurrence occurrence = IncidenceTestBuilder.generateDefaultOccurrence();
    Description description = IncidenceTestBuilder.generateDefaultDescription();
    Classification classification = IncidenceTestBuilder.generateDefaultClassification();

    @Test
    void shouldRejectNullUUID(){
        assertThrows(NullPointerException.class,
                () -> new UpdateIncidenceCommand(null, affectedNetwork, occurrence, description, classification)
        );
    }
}
