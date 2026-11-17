package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineColorsTest {

    private static final HexColor COLOR = new HexColor("FF0000");
    private static final HexColor TEXT_COLOR = new HexColor("FFFFFF");
    private static final HexColor BLACK = new HexColor("000000");
    private static final String EXTERNAL_ID = "L1";
    private static final int CONSECUTIVE_LINES = 10;

    @Test
    void shouldExposeBothColorsWhenArgumentsAreNotNull() {
        LineColors colors = new LineColors(COLOR, TEXT_COLOR);

        assertThat(colors.color()).isEqualTo(COLOR);
        assertThat(colors.textColor()).isEqualTo(TEXT_COLOR);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario, HexColor color, HexColor textColor) {
        assertThatThrownBy(() -> new LineColors(color, textColor))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUseBlackTextWhenDefaultColorsAreGenerated() {
        assertThat(LineColors.defaultFor(EXTERNAL_ID).textColor()).isEqualTo(BLACK);
    }

    @Test
    void shouldGenerateSameDefaultColorsWhenExternalIdIsTheSame() {
        assertThat(LineColors.defaultFor(EXTERNAL_ID)).isEqualTo(LineColors.defaultFor(EXTERNAL_ID));
    }

    @Test
    void shouldGenerateDifferentBackgroundsWhenExternalIdsAreConsecutive() {
        Set<HexColor> backgrounds = IntStream.rangeClosed(1, CONSECUTIVE_LINES)
                .mapToObj(number -> LineColors.defaultFor("C" + number).color())
                .collect(Collectors.toSet());

        assertThat(backgrounds).hasSize(CONSECUTIVE_LINES);
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("color is null", null, TEXT_COLOR),
                Arguments.of("textColor is null", COLOR, null),
                Arguments.of("both are null", null, null)
        );
    }
}
