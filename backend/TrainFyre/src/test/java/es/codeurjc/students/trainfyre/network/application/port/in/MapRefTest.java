package es.codeurjc.students.trainfyre.network.application.port.in;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapRefTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new MapRef.ById(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSourceIsNull() {
        assertThatThrownBy(() -> new MapRef.BySource(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenIdWhenItIsProvided() {
        UUID id = UUID.randomUUID();

        assertThat(new MapRef.ById(id).id()).isEqualTo(id);
    }

    @Test
    void shouldExposeTheGivenSourceWhenItIsProvided() {
        assertThat(new MapRef.BySource(SOURCE).source()).isEqualTo(SOURCE);
    }
}
