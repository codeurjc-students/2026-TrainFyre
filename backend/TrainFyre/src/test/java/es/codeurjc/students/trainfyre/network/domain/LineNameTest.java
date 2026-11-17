package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineNameTest {

    private static final String SHORT_NAME = "L1";
    private static final String LONG_NAME = "Line 1 Pinar de Chamartín - Valdecarros";

    @Test
    void shouldExposeBothNamesWhenBothAreInformed() {
        LineName name = new LineName(SHORT_NAME, LONG_NAME);

        assertThat(name.shortName()).isEqualTo(SHORT_NAME);
        assertThat(name.longName()).isEqualTo(LONG_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("oneMissingName")
    void shouldUseTheOnlyNameForBothFieldsWhenOneIsMissing(String scenario, String shortName, String longName) {
        LineName name = new LineName(shortName, longName);

        assertThat(name.shortName()).isEqualTo(SHORT_NAME);
        assertThat(name.longName()).isEqualTo(SHORT_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bothMissingNames")
    void shouldThrowIllegalArgumentExceptionWhenBothNamesAreMissing(String scenario, String shortName, String longName) {
        assertThatThrownBy(() -> new LineName(shortName, longName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldStripBothNamesWhenTheyHaveSurroundingWhitespace() {
        LineName name = new LineName("  " + SHORT_NAME + "\t", " " + LONG_NAME + "\n");

        assertThat(name.shortName()).isEqualTo(SHORT_NAME);
        assertThat(name.longName()).isEqualTo(LONG_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("oneMissingName")
    void shouldBeEqualWhenOnlyOneNameIsGivenRegardlessOfWhichOne(String scenario, String shortName, String longName) {
        assertThat(new LineName(shortName, longName))
                .isEqualTo(new LineName(SHORT_NAME, SHORT_NAME));
    }

    private static Stream<Arguments> oneMissingName() {
        return Stream.of(
                Arguments.of("longName is null", SHORT_NAME, null),
                Arguments.of("longName is empty", SHORT_NAME, ""),
                Arguments.of("longName is blank", SHORT_NAME, " \t"),
                Arguments.of("shortName is null", null, SHORT_NAME),
                Arguments.of("shortName is empty", "", SHORT_NAME),
                Arguments.of("shortName is blank", " \t", SHORT_NAME),
                Arguments.of("both are the same", SHORT_NAME, SHORT_NAME)
        );
    }

    private static Stream<Arguments> bothMissingNames() {
        return Stream.of(
                Arguments.of("both are null", null, null),
                Arguments.of("both are empty", "", ""),
                Arguments.of("both are blank", " ", "\t\n"),
                Arguments.of("null and blank", null, "  "),
                Arguments.of("empty and null", "", null)
        );
    }
}
