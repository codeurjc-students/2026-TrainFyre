package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransportMapByIdServiceTest {

    private static final MapMetadata METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private DeleteTransportMapByIdUseCase deleteTransportMapByIdUseCase;
    private UUID mapId;

    @BeforeEach
    void setUp() {
        deleteTransportMapByIdUseCase = new DeleteTransportMapByIdService(transportMapPort);
        mapId = TransportMap.createTransportMap(METADATA, CREATED_AT).getId().value();
    }

    @Test
    void shouldFindTheMapAndDeleteItByIdWhenCommandIsValid() {
        givenExistingMap();

        Void result = deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(mapId));

        assertThat(result).isNull();
        verify(transportMapPort).findById(mapId, TransportMap.class);
        verify(transportMapPort).delete(mapId);
    }

    @Test
    void shouldPropagateExceptionWithoutDeletingWhenMapIsNotFound() {
        RuntimeException notFound = new RuntimeException("Transport map not found");
        when(transportMapPort.findById(mapId, TransportMap.class)).thenThrow(notFound);

        assertThatThrownBy(() -> deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(mapId)))
                .isSameAs(notFound);
        verify(transportMapPort, never()).delete(any(UUID.class));
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        givenExistingMap();
        RuntimeException persistenceFailure = new RuntimeException("Could not delete transport map");
        when(transportMapPort.delete(mapId)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(mapId)))
                .isSameAs(persistenceFailure);
    }

    @Test
    void shouldFindTheMapBeforeDeletingIt() {
        givenExistingMap();

        deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(mapId));

        InOrder inOrder = inOrder(transportMapPort);
        inOrder.verify(transportMapPort).findById(mapId, TransportMap.class);
        inOrder.verify(transportMapPort).delete(mapId);
    }

    private void givenExistingMap() {
        when(transportMapPort.findById(mapId, TransportMap.class))
                .thenReturn(TransportMap.createTransportMap(METADATA, CREATED_AT));
    }
}
