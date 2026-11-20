package es.codeurjc.students.trainfyre.network.application.port.in.command;

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

class UpdateTransportMapBySourceCommandTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null source", null, METADATA, NOW),
                Arguments.of("null map metadata", SOURCE, null, NOW),
                Arguments.of("null now", SOURCE, METADATA, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario,
                                                              DataSource source,
                                                              MapMetadata mapMetadata,
                                                              Instant now) {
        assertThatThrownBy(() -> new UpdateTransportMapBySourceCommand(source, mapMetadata, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        UpdateTransportMapBySourceCommand command = new UpdateTransportMapBySourceCommand(SOURCE, METADATA, NOW);

        assertThat(command.source()).isEqualTo(SOURCE);
        assertThat(command.mapMetadata()).isEqualTo(METADATA);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
