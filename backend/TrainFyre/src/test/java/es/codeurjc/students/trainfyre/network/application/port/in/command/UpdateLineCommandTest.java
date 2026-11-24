package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.LineChanges;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateLineCommandTest {

    private static final MapRef MAP = new MapRef.BySource(new DataSource("renfe", "cercanias-madrid"));
    private static final LineRef LINE = new LineRef.ByExternalId("C-1");
    private static final LineChanges CHANGES = new LineChanges(new LineName("C1", "Cercanias C1"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map", null, LINE, CHANGES, NOW),
                Arguments.of("null line", MAP, null, CHANGES, NOW),
                Arguments.of("null changes", MAP, LINE, null, NOW),
                Arguments.of("null now", MAP, LINE, CHANGES, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario,
                                                              MapRef map,
                                                              LineRef line,
                                                              LineChanges changes,
                                                              Instant now) {
        assertThatThrownBy(() -> new UpdateLineCommand(map, line, changes, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        UpdateLineCommand command = new UpdateLineCommand(MAP, LINE, CHANGES, NOW);

        assertThat(command.map()).isEqualTo(MAP);
        assertThat(command.line()).isEqualTo(LINE);
        assertThat(command.changes()).isEqualTo(CHANGES);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
