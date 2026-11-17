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

class LineTest {

    private static final LineId ID = new LineId(1L);
    private static final String EXTERNAL_ID = "L1";
    private static final LineName NAME = new LineName("L1", "Line 1");
    private static final LineType TYPE = LineType.METRO;
    private static final LineColors COLORS = new LineColors(new HexColor("FF0000"), new HexColor("FFFFFF"));
    private static final HexColor BLACK = new HexColor("000000");
    private static final int CONSECUTIVE_LINES = 10;

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenRequiredArgumentIsNull(
            String scenario, String externalId, LineName name, LineType type) {
        assertThatThrownBy(() -> Line.createLine(externalId, name, type, COLORS))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @MethodSource("blankExternalIds")
    void shouldThrowIllegalArgumentExceptionWhenExternalIdIsEmptyOrBlank(String externalId) {
        assertThatThrownBy(() -> Line.createLine(externalId, NAME, TYPE, COLORS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @MethodSource("paddedExternalIds")
    void shouldStripExternalIdWhenItHasSurroundingWhitespace(String externalId) {
        Line line = Line.createLine(externalId, NAME, TYPE, COLORS);

        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReconstitutionNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyReconstitutionArgumentIsNull(
            String scenario, LineId id, String externalId, LineName name, LineType type, LineColors colors) {
        assertThatThrownBy(() -> Line.reconstitute(id, externalId, name, type, colors))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @MethodSource("blankExternalIds")
    void shouldThrowIllegalArgumentExceptionWhenReconstitutedExternalIdIsEmptyOrBlank(String externalId) {
        assertThatThrownBy(() -> Line.reconstitute(ID, externalId, NAME, TYPE, COLORS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @MethodSource("paddedExternalIds")
    void shouldStripExternalIdWhenReconstitutingWithSurroundingWhitespace(String externalId) {
        Line line = Line.reconstitute(ID, externalId, NAME, TYPE, COLORS);

        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
    }

    @Test
    void shouldKeepGivenIdAndValuesWhenReconstituting() {
        Line line = Line.reconstitute(ID, EXTERNAL_ID, NAME, TYPE, COLORS);

        assertThat(line.getId()).isEqualTo(ID);
        assertThat(line.getExternalId()).isEqualTo(EXTERNAL_ID);
        assertThat(line.getName()).isEqualTo(NAME);
        assertThat(line.getType()).isEqualTo(TYPE);
        assertThat(line.getColors()).isEqualTo(COLORS);
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

    @Test
    void shouldGenerateColorsWithBlackTextWhenColorsIsNull() {
        Line line = lineWithoutColors(EXTERNAL_ID);

        assertThat(line.getColors()).isNotNull();
        assertThat(line.getColors().textColor()).isEqualTo(BLACK);
    }

    @Test
    void shouldGenerateSameColorsWhenExternalIdIsTheSame() {
        LineColors first = lineWithoutColors(EXTERNAL_ID).getColors();
        LineColors second = lineWithoutColors(EXTERNAL_ID).getColors();

        assertThat(first).isNotNull().isEqualTo(second);
    }

    @Test
    void shouldGenerateSameColorsWhenExternalIdOnlyDiffersInSurroundingWhitespace() {
        LineColors stripped = lineWithoutColors(EXTERNAL_ID).getColors();
        LineColors padded = lineWithoutColors("  " + EXTERNAL_ID + "\t").getColors();

        assertThat(stripped).isNotNull().isEqualTo(padded);
    }

    @Test
    void shouldGenerateDifferentBackgroundColorsWhenExternalIdsAreConsecutive() {
        Set<HexColor> backgrounds = IntStream.rangeClosed(1, CONSECUTIVE_LINES)
                .mapToObj(number -> lineWithoutColors("C" + number).getColors())
                .map(colors -> colors == null ? null : colors.color())
                .collect(Collectors.toSet());

        assertThat(backgrounds).doesNotContainNull().hasSize(CONSECUTIVE_LINES);
    }

    private static Line lineWithoutColors(String externalId) {
        return Line.createLine(externalId, NAME, TYPE, null);
    }

    private static Stream<String> blankExternalIds() {
        return Stream.of("", " ", "   ", "\t", "\n", " \t\n ");
    }

    private static Stream<String> paddedExternalIds() {
        return Stream.of(" L1", "L1 ", "  L1\t", "\nL1\n");
    }

    private static Stream<Arguments> invalidReconstitutionNullArguments() {
        return Stream.of(
                Arguments.of("id is null", null, EXTERNAL_ID, NAME, TYPE, COLORS),
                Arguments.of("externalId is null", ID, null, NAME, TYPE, COLORS),
                Arguments.of("name is null", ID, EXTERNAL_ID, null, TYPE, COLORS),
                Arguments.of("type is null", ID, EXTERNAL_ID, NAME, null, COLORS),
                Arguments.of("colors is null", ID, EXTERNAL_ID, NAME, TYPE, null)
        );
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("externalId is null", null, NAME, TYPE),
                Arguments.of("name is null", EXTERNAL_ID, null, TYPE),
                Arguments.of("type is null", EXTERNAL_ID, NAME, null)
        );
    }
}
