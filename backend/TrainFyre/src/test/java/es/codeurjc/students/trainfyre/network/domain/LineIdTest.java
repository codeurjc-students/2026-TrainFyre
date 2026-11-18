package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineIdTest {

    private static final Long VALUE = 1L;

    @Test
    void shouldExposeValueWhenValueIsNotNull() {
        assertThat(new LineId(VALUE).value()).isEqualTo(VALUE);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new LineId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
