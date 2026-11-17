package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapMetadataTest {

    private static final String NAME = "Cercanias Madrid";
    private static final ZoneId TIMEZONE = ZoneId.of("Europe/Madrid");
    private static final String COUNTRY = "ES";
    private static final URI WEBSITE = URI.create("https://www.renfe.com");
    private static final List<String> BLANK_VALUES = List.of("", " ", "   ", "\t", "\n", " \t\n ");

    @Test
    void shouldExposeAllValuesWhenArgumentsAreValid() {
        MapMetadata metadata = new MapMetadata(NAME, TIMEZONE, COUNTRY, WEBSITE);

        assertThat(metadata.name()).isEqualTo(NAME);
        assertThat(metadata.timezone()).isEqualTo(TIMEZONE);
        assertThat(metadata.country()).isEqualTo(COUNTRY);
        assertThat(metadata.website()).isEqualTo(WEBSITE);
    }

    @Test
    void shouldAllowNullCountryAndWebsiteWhenTheyAreOptional() {
        MapMetadata metadata = new MapMetadata(NAME, TIMEZONE, null, null);

        assertThat(metadata.country()).isNull();
        assertThat(metadata.website()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenNameOrTimezoneIsNull(String scenario, String name, ZoneId timezone) {
        assertThatThrownBy(() -> new MapMetadata(name, timezone, COUNTRY, WEBSITE))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "name = \"{0}\"")
    @MethodSource("blankNames")
    void shouldThrowIllegalArgumentExceptionWhenNameIsEmptyOrBlank(String name) {
        assertThatThrownBy(() -> new MapMetadata(name, TIMEZONE, COUNTRY, WEBSITE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "name = \"{0}\"")
    @ValueSource(strings = {" Cercanias Madrid", "Cercanias Madrid ", "  Cercanias Madrid\t", "\nCercanias Madrid\n"})
    void shouldStripNameWhenItHasSurroundingWhitespace(String name) {
        assertThat(new MapMetadata(name, TIMEZONE, COUNTRY, WEBSITE).name()).isEqualTo(NAME);
    }

    @ParameterizedTest(name = "country = \"{0}\"")
    @ValueSource(strings = {"es", "Es", "eS", "ES"})
    void shouldNormalizeCountryToUppercaseWhenItIsAValidIsoAlpha2Code(String country) {
        assertThat(new MapMetadata(NAME, TIMEZONE, country, WEBSITE).country()).isEqualTo(COUNTRY);
    }

    @ParameterizedTest(name = "country = \"{0}\"")
    @ValueSource(strings = {"ESP", "E", "ZZ", "12", "E1"})
    void shouldThrowIllegalArgumentExceptionWhenCountryIsNotAnIsoAlpha2Code(String country) {
        assertThatThrownBy(() -> new MapMetadata(NAME, TIMEZONE, country, WEBSITE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "website = {0}")
    @ValueSource(strings = {"http://www.renfe.com", "https://www.renfe.com/es/cercanias"})
    void shouldAcceptWebsiteWhenSchemeIsHttpOrHttpsAndHasHost(String website) {
        URI uri = URI.create(website);

        assertThat(new MapMetadata(NAME, TIMEZONE, COUNTRY, uri).website()).isEqualTo(uri);
    }

    @ParameterizedTest(name = "website = {0}")
    @MethodSource("invalidWebsites")
    void shouldThrowIllegalArgumentExceptionWhenWebsiteIsNotAnHttpOrHttpsUrlWithHost(URI website) {
        assertThatThrownBy(() -> new MapMetadata(NAME, TIMEZONE, COUNTRY, website))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("name is null", null, TIMEZONE),
                Arguments.of("timezone is null", NAME, null),
                Arguments.of("both are null", null, null)
        );
    }

    private static Stream<String> blankNames() {
        return BLANK_VALUES.stream();
    }

    private static Stream<URI> invalidWebsites() {
        return Stream.of(
                URI.create("ftp://www.renfe.com"),
                URI.create("mailto:info@renfe.com"),
                URI.create("www.renfe.com"),
                URI.create("/relative/path"),
                URI.create("https:///path")
        );
    }
}
