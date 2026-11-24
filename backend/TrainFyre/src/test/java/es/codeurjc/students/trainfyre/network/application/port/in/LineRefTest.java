package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.LineId;
import org.junit.jupiter.api.Test;

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

    @Test
    void shouldExposeTheGivenIdWhenItIsProvided() {
        assertThat(new LineRef.ById(LINE_ID).id()).isEqualTo(LINE_ID);
    }

    @Test
    void shouldExposeTheGivenExternalIdWhenItIsProvided() {
        assertThat(new LineRef.ByExternalId(EXTERNAL_ID).externalId()).isEqualTo(EXTERNAL_ID);
    }
}
