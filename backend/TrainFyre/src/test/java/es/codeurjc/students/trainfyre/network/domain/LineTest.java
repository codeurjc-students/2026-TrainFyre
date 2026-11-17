package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineTest {

    private static final String EXTERNAL_ID = "L1";
    private static final LineName NAME = new LineName("L1", "Line 1");
    private static final LineType TYPE = LineType.METRO;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenRequiredArgumentIsNull(
            String scenario, String externalId, LineName name, LineType type) {
        assertThatThrownBy(() -> Line.createLine(externalId, name, type, COLORS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCreateLineWithGivenValuesAndWithoutIdWhenColorsAreInformed() {
        Line line = Line.createLine(EXTERNAL_ID, NAME, TYPE, COLORS);

        assertThat(line.getId()).isNull();
        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(line.getName()).isEqualTo(NAME);
        assertThat(line.getType()).isEqualTo(TYPE);
        assertThat(line.getColors()).isEqualTo(COLORS);
    }

    @Test
    void shouldCreateLineKeepingRequiredValuesWhenColorsIsNull() {
        Line line = Line.createLine(EXTERNAL_ID, NAME, TYPE, null);

        assertThat(line.getId()).isNull();
        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(line.getName()).isEqualTo(NAME);
        assertThat(line.getType()).isEqualTo(TYPE);
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("externalId is null", null, NAME, TYPE),
                Arguments.of("name is null", EXTERNAL_ID, null, TYPE),
                Arguments.of("type is null", EXTERNAL_ID, NAME, null)
        );
    }
}
