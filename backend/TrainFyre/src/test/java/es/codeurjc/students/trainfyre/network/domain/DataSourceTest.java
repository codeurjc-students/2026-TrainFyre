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

    private static final String PROVIDER = "renfe";
    private static final String EXTERNAL_ID = "renfe-cercanias";
    private static final String MIXED_CASE_EXTERNAL_ID = "Cercanias";
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

    @ParameterizedTest(name = "provider = \"{0}\"")
    @MethodSource("unnormalizedProviders")
    void shouldStripAndLowercaseProviderWhenItHasSurroundingWhitespaceOrUppercase(String provider) {
        assertThat(new DataSource(provider, EXTERNAL_ID).provider()).isEqualTo(PROVIDER);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @MethodSource("paddedExternalIds")
    void shouldStripExternalIdAndKeepItsCaseWhenItHasSurroundingWhitespace(String externalId) {
        assertThat(new DataSource(PROVIDER, externalId).externalId()).isEqualTo(MIXED_CASE_EXTERNAL_ID);
    }

    @Test
    void shouldBeEqualWhenProvidersOnlyDifferInCaseOrSurroundingWhitespace() {
        assertThat(new DataSource("  Renfe\t", EXTERNAL_ID)).isEqualTo(new DataSource(PROVIDER, EXTERNAL_ID));
    }

    private static Stream<String> unnormalizedProviders() {
        return Stream.of(" renfe", "renfe ", "  renfe\t", "\nrenfe\n", "Renfe", "RENFE", " ReNfE ");
    }

    private static Stream<String> paddedExternalIds() {
        return Stream.of(" Cercanias", "Cercanias ", "  Cercanias\t", "\nCercanias\n");
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
