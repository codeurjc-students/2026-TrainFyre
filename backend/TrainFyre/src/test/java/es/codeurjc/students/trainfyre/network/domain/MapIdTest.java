package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapIdTest {

    private static final UUID VALUE = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void shouldExposeValueWhenValueIsNotNull() {
        assertThat(new MapId(VALUE).value()).isEqualTo(VALUE);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new MapId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
