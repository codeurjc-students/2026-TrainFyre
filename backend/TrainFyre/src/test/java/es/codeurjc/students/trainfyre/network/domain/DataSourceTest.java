package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceTest {

    private static final String PROVIDER = "gtfs";
    private static final String EXTERNAL_ID = "renfe-cercanias";
    private static final List<String> BLANK_VALUES = List.of("", " ", "   ", "\t", "\n", " \t\n ");

    @Test
    void shouldExposeBothValuesWhenArgumentsAreNotNull() {
        DataSource source = new DataSource(PROVIDER, EXTERNAL_ID);

        assertThat(source.provider()).isEqualTo(PROVIDER);
        assertThat(source.externalId()).isEqualTo(EXTERNAL_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario, String provider, String externalId) {
        assertThatThrownBy(() -> new DataSource(provider, externalId))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blankArguments")
    void shouldThrowIllegalArgumentExceptionWhenAnyArgumentIsEmptyOrBlank(
            String scenario, String provider, String externalId) {
        assertThatThrownBy(() -> new DataSource(provider, externalId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> blankArguments() {
        return BLANK_VALUES.stream().flatMap(blank -> Stream.of(
                Arguments.of("provider = \"" + blank + "\"", blank, EXTERNAL_ID),
                Arguments.of("externalId = \"" + blank + "\"", PROVIDER, blank)
        ));
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("provider is null", null, EXTERNAL_ID),
                Arguments.of("externalId is null", PROVIDER, null),
                Arguments.of("both are null", null, null)
        );
    }
}
