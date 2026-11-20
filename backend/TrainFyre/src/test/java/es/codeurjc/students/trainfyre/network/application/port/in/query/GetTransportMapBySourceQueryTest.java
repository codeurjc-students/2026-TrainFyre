package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetTransportMapBySourceQueryTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");

    @Test
    void shouldThrowNullPointerExceptionWhenSourceIsNull() {
        assertThatThrownBy(() -> new GetTransportMapBySourceQuery(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenSourceWhenItIsProvided() {
        GetTransportMapBySourceQuery query = new GetTransportMapBySourceQuery(SOURCE);

        assertThat(query.dataSource()).isEqualTo(SOURCE);
    }
}
