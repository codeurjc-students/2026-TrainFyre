package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateTransportMapCommandTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapRef MAP = new MapRef.BySource(SOURCE);
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map", null, METADATA, NOW),
                Arguments.of("null map metadata", MAP, null, NOW),
                Arguments.of("null now", MAP, METADATA, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario,
                                                              MapRef map,
                                                              MapMetadata mapMetadata,
                                                              Instant now) {
        assertThatThrownBy(() -> new UpdateTransportMapCommand(map, mapMetadata, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        UpdateTransportMapCommand command = new UpdateTransportMapCommand(MAP, METADATA, NOW);

        assertThat(command.map()).isEqualTo(MAP);
        assertThat(command.mapMetadata()).isEqualTo(METADATA);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
