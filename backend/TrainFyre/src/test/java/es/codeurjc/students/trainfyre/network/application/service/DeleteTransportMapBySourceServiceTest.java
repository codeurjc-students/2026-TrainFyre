package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransportMapBySourceServiceTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private DeleteTransportMapBySourceUseCase deleteTransportMapBySourceUseCase;
    private TransportMap existingMap;
    private UUID mapId;

    @BeforeEach
    void setUp() {
        deleteTransportMapBySourceUseCase = new DeleteTransportMapBySourceService(transportMapPort, new MapSourcePolicy(transportMapPort));
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
        mapId = existingMap.getId().value();
    }

    @Test
    void shouldDeleteTheMapFoundBySourceWhenCommandIsValid() {
        givenExistingMap();

        Void result = deleteTransportMapBySourceUseCase.execute(new DeleteTransportMapBySourceCommand(SOURCE));

        assertThat(result).isNull();
        verify(transportMapPort).delete(mapId);
    }

    @Test
    void shouldThrowMapNotFoundExceptionWithoutDeletingWhenNoMapHasTheSource() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteTransportMapBySourceUseCase.execute(new DeleteTransportMapBySourceCommand(SOURCE)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).delete(any(UUID.class));
    }

    @Test
    void shouldPropagateExceptionWithoutDeletingWhenSourceLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> deleteTransportMapBySourceUseCase.execute(new DeleteTransportMapBySourceCommand(SOURCE)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).delete(any(UUID.class));
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        givenExistingMap();
        RuntimeException persistenceFailure = new RuntimeException("Could not delete transport map");
        when(transportMapPort.delete(mapId)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> deleteTransportMapBySourceUseCase.execute(new DeleteTransportMapBySourceCommand(SOURCE)))
                .isSameAs(persistenceFailure);
    }

    @Test
    void shouldFindTheMapBySourceBeforeDeletingIt() {
        givenExistingMap();

        deleteTransportMapBySourceUseCase.execute(new DeleteTransportMapBySourceCommand(SOURCE));

        InOrder inOrder = inOrder(transportMapPort);
        inOrder.verify(transportMapPort).findBySource(SOURCE);
        inOrder.verify(transportMapPort).delete(mapId);
    }

    private void givenExistingMap() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.of(existingMap));
    }
}
