package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineChangesTest {

    private static final LineName NAME = new LineName("L2", "Line 2");
    private static final LineType TYPE = LineType.BUS;
    private static final LineColors COLORS = new LineColors(new HexColor("00FF00"), new HexColor("000000"));

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAllChangesAreNull() {
        assertThatThrownBy(() -> new LineChanges(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("atLeastOneChange")
    void shouldExposeGivenValuesWhenAtLeastOneChangeIsGiven(
            String scenario, LineName name, LineType type, LineColors colors) {
        LineChanges changes = new LineChanges(name, type, colors);

        assertThat(changes.name()).isEqualTo(name);
        assertThat(changes.type()).isEqualTo(type);
        assertThat(changes.colors()).isEqualTo(colors);
    }

    private static Stream<Arguments> atLeastOneChange() {
        return Stream.of(
                Arguments.of("only name", NAME, null, null),
                Arguments.of("only type", null, TYPE, null),
                Arguments.of("only colors", null, null, COLORS),
                Arguments.of("name and type", NAME, TYPE, null),
                Arguments.of("name and colors", NAME, null, COLORS),
                Arguments.of("type and colors", null, TYPE, COLORS),
                Arguments.of("all three", NAME, TYPE, COLORS)
        );
    }
}
