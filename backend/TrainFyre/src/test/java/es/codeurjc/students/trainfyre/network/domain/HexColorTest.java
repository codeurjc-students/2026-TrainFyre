package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexColorTest {

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new HexColor(null))
                .isInstanceOf(NullPointerException.class);
    }
}
