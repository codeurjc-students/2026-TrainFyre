package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.common.Pageable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAllTransportMapsSlicedQueryTest {

    private static final Pageable PAGEABLE = new Pageable(0, 20);

    @Test
    void shouldThrowNullPointerExceptionWhenPageableIsNull() {
        assertThatThrownBy(() -> new GetAllTransportMapsSlicedQuery(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenPageableWhenItIsProvided() {
        assertThat(new GetAllTransportMapsSlicedQuery(PAGEABLE).pageable()).isEqualTo(PAGEABLE);
    }
}
