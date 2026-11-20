package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.GetTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.query.GetTransportMapByIdQuery;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransportMapByIdServiceTest {

    private static final MapMetadata METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private GetTransportMapByIdUseCase getTransportMapByIdUseCase;
    private TransportMap existingMap;
    private UUID mapId;

    @BeforeEach
    void setUp() {
        getTransportMapByIdUseCase = new GetTransportMapByIdService(transportMapPort);
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
        mapId = existingMap.getId().value();
    }

    @Test
    void shouldReturnTheMapFoundByIdWhenQueryIsValid() {
        when(transportMapPort.findById(mapId, TransportMap.class)).thenReturn(existingMap);

        TransportMap result = getTransportMapByIdUseCase.execute(new GetTransportMapByIdQuery(mapId));

        assertThat(result).isSameAs(existingMap);
    }

    @Test
    void shouldPropagateExceptionWhenTheLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Transport map not found");
        when(transportMapPort.findById(mapId, TransportMap.class)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> getTransportMapByIdUseCase.execute(new GetTransportMapByIdQuery(mapId)))
                .isSameAs(lookupFailure);
    }
}
