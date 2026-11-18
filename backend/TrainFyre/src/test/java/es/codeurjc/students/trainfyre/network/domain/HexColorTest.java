package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexColorTest {

    private static final String RED = "FF0000";

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new HexColor(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "\"{0}\" is normalized to \"{1}\"")
    @CsvSource({
            "FF0000, FF0000",
            "ff0000, FF0000",
            "fF00aB, FF00AB",
            "'#ff0000', FF0000",
            "'#FF0000', FF0000",
            "' ff0000 ', FF0000",
            "' #Ff0000\t', FF0000"
    })
    void shouldNormalizeToUpperCaseWithoutHashWhenValueIsValidHexColor(String input, String expected) {
        assertThat(new HexColor(input).value()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @ValueSource(strings = {
            "",
            "   ",
            "#",
            "F00",
            "FF00",
            "FF00000",
            "GG0000",
            "FF 000",
            "##FF0000",
            "0xFF0000",
            "FF000Z"
    })
    void shouldThrowIllegalArgumentExceptionWhenValueIsNotSixHexDigits(String input) {
        assertThatThrownBy(() -> new HexColor(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualWhenValuesOnlyDifferInFormat() {
        assertThat(new HexColor("#ff0000 ")).isEqualTo(new HexColor(RED));
    }
}
