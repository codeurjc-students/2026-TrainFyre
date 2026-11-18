package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransportMapTest {

    private static final MapMetadata METADATA =
            new MapMetadata("Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);

    @Test
    void shouldThrowNullPointerExceptionWhenMetadataIsNull() {
        assertThatThrownBy(() -> TransportMap.createTransportMap(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldGenerateRandomIdWhenMapIsCreated() {
        TransportMap first = TransportMap.createTransportMap(METADATA);
        TransportMap second = TransportMap.createTransportMap(METADATA);

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void shouldHaveNullLastUpdatedAtWhenMapIsCreated() {
        assertThat(TransportMap.createTransportMap(METADATA).getLastUpdatedAt()).isNull();
    }

    @Test
    void shouldHaveEmptyLineIndexesWhenMapIsCreated() {
        TransportMap map = TransportMap.createTransportMap(METADATA);

        assertThat(map.getLines()).isNotNull().isEmpty();
        assertThat(map.getLinesByExternalId()).isNotNull().isEmpty();
    }
}
