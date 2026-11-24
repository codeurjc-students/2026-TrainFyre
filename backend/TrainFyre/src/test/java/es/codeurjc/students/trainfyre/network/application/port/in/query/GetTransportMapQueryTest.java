package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetTransportMapQueryTest {

    private static final MapRef MAP = new MapRef.BySource(new DataSource("renfe", "cercanias-madrid"));

    @Test
    void shouldThrowNullPointerExceptionWhenMapIsNull() {
        assertThatThrownBy(() -> new GetTransportMapQuery(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenMapWhenItIsProvided() {
        assertThat(new GetTransportMapQuery(MAP).map()).isEqualTo(MAP);
    }
}
