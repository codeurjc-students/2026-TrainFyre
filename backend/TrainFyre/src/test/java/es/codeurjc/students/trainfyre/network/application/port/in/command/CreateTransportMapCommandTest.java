package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.Line;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateTransportMapCommandTest {

    private static final MapMetadata METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Collection<Line> LINES = List.of();
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map metadata", null, LINES, NOW),
                Arguments.of("null now", METADATA, LINES, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenMapMetadataOrNowIsNull(String scenario,
                                                                   MapMetadata mapMetadata,
                                                                   Collection<Line> lines,
                                                                   Instant now) {
        assertThatThrownBy(() -> new CreateTransportMapCommand(mapMetadata, lines, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNormalizeNullLinesToEmptyCollection() {
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, null, NOW);

        assertThat(command.lines()).isNotNull().isEmpty();
    }

    static Stream<Arguments> linesSources() {
        return Stream.of(
                Arguments.of("null lines", null),
                Arguments.of("mutable lines", new ArrayList<Line>())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("linesSources")
    void shouldExposeImmutableLines(String scenario, Collection<Line> lines) {
        CreateTransportMapCommand command = new CreateTransportMapCommand(METADATA, lines, NOW);

        assertThatThrownBy(() -> command.lines().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
