package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateTransportMapByIdCommandTest {

    private static final UUID ID = UUID.fromString("b3f1c2d4-5e6a-4b7c-8d9e-0a1b2c3d4e5f");
    private static final MapMetadata METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null uuid", null, METADATA, NOW),
                Arguments.of("null map metadata", ID, null, NOW),
                Arguments.of("null now", ID, METADATA, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario,
                                                              UUID uuid,
                                                              MapMetadata mapMetadata,
                                                              Instant now) {
        assertThatThrownBy(() -> new UpdateTransportMapByIdCommand(uuid, mapMetadata, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        UpdateTransportMapByIdCommand command = new UpdateTransportMapByIdCommand(ID, METADATA, NOW);

        assertThat(command.uuid()).isEqualTo(ID);
        assertThat(command.mapMetadata()).isEqualTo(METADATA);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
