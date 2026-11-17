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
    void shouldExposeBothNamesWhenArgumentsAreNotNull() {
        LineName name = new LineName(SHORT_NAME, LONG_NAME);

        assertThat(name.shortName()).isEqualTo(SHORT_NAME);
        assertThat(name.longName()).isEqualTo(LONG_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario, String shortName, String longName) {
        assertThatThrownBy(() -> new LineName(shortName, longName))
                .isInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("shortName is null", null, LONG_NAME),
                Arguments.of("longName is null", SHORT_NAME, null),
                Arguments.of("both are null", null, null)
        );
    }
}
