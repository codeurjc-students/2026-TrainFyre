package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapBySourceQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransportMapBySourceServiceTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetTransportMapBySourceUseCase getTransportMapBySourceUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        getTransportMapBySourceUseCase = new GetTransportMapBySourceService(transportMapPort);
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
    }

    @Test
    void shouldReturnTheMapFoundBySourceWhenQueryIsValid() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.of(existingMap));

        TransportMap result = getTransportMapBySourceUseCase.execute(new GetTransportMapBySourceQuery(SOURCE));

        assertThat(result).isSameAs(existingMap);
    }

    @Test
    void shouldThrowMapNotFoundExceptionWhenNoMapHasTheSource() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getTransportMapBySourceUseCase.execute(new GetTransportMapBySourceQuery(SOURCE)))
                .isInstanceOf(MapNotFoundException.class);
    }

    @Test
    void shouldPropagateExceptionWhenTheLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> getTransportMapBySourceUseCase.execute(new GetTransportMapBySourceQuery(SOURCE)))
                .isSameAs(lookupFailure);
    }
}
