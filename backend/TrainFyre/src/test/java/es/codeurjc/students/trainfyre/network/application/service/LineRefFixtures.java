package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.domain.LineId;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_ID;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.BY_SOURCE;

/**
 * Shared by the service tests that receive a {@link LineRef}: both ways of referencing the same persisted
 * line, and references to lines that do not exist.
 */
final class LineRefFixtures {

    static final LineId LINE_ID = new LineId(7L);
    static final String EXTERNAL_ID = "C-1";
    static final LineRef LINE_BY_ID = new LineRef.ById(LINE_ID);
    static final LineRef LINE_BY_EXTERNAL_ID = new LineRef.ByExternalId(EXTERNAL_ID);

    private LineRefFixtures() {
    }

    static Stream<Arguments> mapAndLineRefs() {
        return Stream.of(
                Arguments.of("map by id, line by id", BY_ID, LINE_BY_ID),
                Arguments.of("map by id, line by external id", BY_ID, LINE_BY_EXTERNAL_ID),
                Arguments.of("map by source, line by id", BY_SOURCE, LINE_BY_ID),
                Arguments.of("map by source, line by external id", BY_SOURCE, LINE_BY_EXTERNAL_ID)
        );
    }

    static Stream<Arguments> existingLineRefs() {
        return Stream.of(
                Arguments.of("line by id", LINE_BY_ID),
                Arguments.of("line by external id", LINE_BY_EXTERNAL_ID)
        );
    }

    static Stream<Arguments> unknownLineRefs() {
        return Stream.of(
                Arguments.of("unknown id", new LineRef.ById(new LineId(99L))),
                Arguments.of("unknown external id", new LineRef.ByExternalId("X-9"))
        );
    }
}
