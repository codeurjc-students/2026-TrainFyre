package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.HexColor;
import es.codeurjc.students.trainfyre.network.domain.LineColors;
import es.codeurjc.students.trainfyre.network.domain.LineName;
import es.codeurjc.students.trainfyre.network.domain.LineType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateLineCommandTest {

    private static final MapRef MAP = new MapRef.BySource(new DataSource("renfe", "cercanias-madrid"));
    private static final String EXTERNAL_ID = "C-1";
    private static final LineName NAME = new LineName("C1", "Cercanias C1");
    private static final LineType TYPE = LineType.RAIL;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map", null, EXTERNAL_ID, NAME, TYPE, NOW),
                Arguments.of("null external id", MAP, null, NAME, TYPE, NOW),
                Arguments.of("null name", MAP, EXTERNAL_ID, null, TYPE, NOW),
                Arguments.of("null type", MAP, EXTERNAL_ID, NAME, null, NOW),
                Arguments.of("null now", MAP, EXTERNAL_ID, NAME, TYPE, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyRequiredArgumentIsNull(String scenario,
                                                                      MapRef map,
                                                                      String externalId,
                                                                      LineName name,
                                                                      LineType type,
                                                                      Instant now) {
        assertThatThrownBy(() -> new CreateLineCommand(map, externalId, name, type, COLORS, now))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", " \t\n "})
    void shouldThrowIllegalArgumentExceptionWhenExternalIdIsEmptyOrBlank(String externalId) {
        assertThatThrownBy(() -> new CreateLineCommand(MAP, externalId, NAME, TYPE, COLORS, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @ValueSource(strings = {" C-1", "C-1 ", "  C-1\t", "\nC-1\n"})
    void shouldStripExternalIdWhenItHasSurroundingWhitespace(String externalId) {
        CreateLineCommand command = new CreateLineCommand(MAP, externalId, NAME, TYPE, COLORS, NOW);

        assertThat(command.externalId()).isEqualTo(EXTERNAL_ID);
    }

    @Test
    void shouldKeepExternalIdCaseWhenItIsStripped() {
        CreateLineCommand command = new CreateLineCommand(MAP, " c-1 ", NAME, TYPE, COLORS, NOW);

        assertThat(command.externalId()).isEqualTo("c-1");
    }

    @Test
    void shouldAcceptNullColorsWhenTheyAreNotProvided() {
        CreateLineCommand command = new CreateLineCommand(MAP, EXTERNAL_ID, NAME, TYPE, null, NOW);

        assertThat(command.colors()).isNull();
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenAllAreProvided() {
        CreateLineCommand command = new CreateLineCommand(MAP, EXTERNAL_ID, NAME, TYPE, COLORS, NOW);

        assertThat(command.map()).isEqualTo(MAP);
        assertThat(command.externalId()).isEqualTo(EXTERNAL_ID);
        assertThat(command.name()).isEqualTo(NAME);
        assertThat(command.type()).isEqualTo(TYPE);
        assertThat(command.colors()).isEqualTo(COLORS);
        assertThat(command.now()).isEqualTo(NOW);
    }
}
