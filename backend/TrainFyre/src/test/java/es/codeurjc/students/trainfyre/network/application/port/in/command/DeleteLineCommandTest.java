package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteLineCommandTest {

    private static final MapRef MAP = new MapRef.BySource(new DataSource("renfe", "cercanias-madrid"));
    private static final LineRef LINE = new LineRef.ByExternalId("C-1");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map", null, LINE, NOW),
                Arguments.of("null line", MAP, null, NOW),
                Arguments.of("null now", MAP, LINE, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario,
                                                              MapRef map,
                                                              LineRef line,
                                                              Instant now) {
        assertThatThrownBy(() -> new DeleteLineCommand(map, line, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        DeleteLineCommand command = new DeleteLineCommand(MAP, LINE, NOW);

        assertThat(command.map()).isEqualTo(MAP);
        assertThat(command.line()).isEqualTo(LINE);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
