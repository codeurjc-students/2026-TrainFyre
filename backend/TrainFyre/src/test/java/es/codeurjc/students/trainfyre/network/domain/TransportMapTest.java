package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportMapTest {

    @Test
    void shouldThrowNullPointerExceptionWhenMetadataIsNull() {
        assertThatThrownBy(() -> TransportMap.createTransportMap(null))
                .isInstanceOf(NullPointerException.class);
    }
}
