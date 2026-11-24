package es.codeurjc.students.trainfyre.network.application.port.in.ref;

import es.codeurjc.students.trainfyre.network.domain.LineId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineRefTest {

    private static final LineId LINE_ID = new LineId(7L);
    private static final String EXTERNAL_ID = "C-1";

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new LineRef.ById(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenExternalIdIsNull() {
        assertThatThrownBy(() -> new LineRef.ByExternalId(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @ValueSource(strings = {"", " ", "   ", "\t", "\n", " \t\n "})
    void shouldThrowIllegalArgumentExceptionWhenExternalIdIsEmptyOrBlank(String externalId) {
        assertThatThrownBy(() -> new LineRef.ByExternalId(externalId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldExposeTheGivenIdWhenItIsProvided() {
        assertThat(new LineRef.ById(LINE_ID).id()).isEqualTo(LINE_ID);
    }

    @Test
    void shouldExposeTheGivenExternalIdWhenItIsProvided() {
        assertThat(new LineRef.ByExternalId(EXTERNAL_ID).externalId()).isEqualTo(EXTERNAL_ID);
    }

    @ParameterizedTest(name = "externalId = \"{0}\"")
    @ValueSource(strings = {" C-1", "C-1 ", "  C-1\t", "\nC-1\n"})
    void shouldStripExternalIdWhenItHasSurroundingWhitespace(String externalId) {
        assertThat(new LineRef.ByExternalId(externalId).externalId()).isEqualTo(EXTERNAL_ID);
    }

    @Test
    void shouldKeepExternalIdCaseWhenItIsStripped() {
        assertThat(new LineRef.ByExternalId(" c-1 ").externalId()).isEqualTo("c-1");
    }

    @Test
    void shouldBeEqualWhenExternalIdsDifferOnlyInSurroundingWhitespace() {
        assertThat(new LineRef.ByExternalId(" C-1 ")).isEqualTo(new LineRef.ByExternalId(EXTERNAL_ID));
    }
}
