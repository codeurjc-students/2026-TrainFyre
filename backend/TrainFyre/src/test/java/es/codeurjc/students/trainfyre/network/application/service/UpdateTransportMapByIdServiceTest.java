package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTransportMapByIdServiceTest {

    private static final MapMetadata CURRENT_METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-madrid"), "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata NEW_METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-valencia"), "Cercanias Valencia", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata RENAMED_METADATA = new MapMetadata(
            CURRENT_METADATA.source(), "Cercanias Madrid Centro", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private UpdateTransportMapByIdUseCase updateTransportMapByIdUseCase;
    private TransportMap existingMap;
    private UUID mapId;

    @BeforeEach
    void setUp() {
        updateTransportMapByIdUseCase = new UpdateTransportMapByIdService(transportMapPort);
        existingMap = TransportMap.createTransportMap(CURRENT_METADATA, CREATED_AT);
        mapId = existingMap.getId().value();
    }

    @Test
    void shouldReplaceMetadataAndSaveTheMapWhenCommandIsValid() {
        givenExistingMap();

        Void result = updateTransportMapByIdUseCase.execute(new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW));

        TransportMap savedMap = captureSavedMap();
        assertThat(result).isNull();
        assertThat(savedMap).isSameAs(existingMap);
        assertThat(savedMap.getMapMetadata()).isEqualTo(NEW_METADATA);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldSaveTheMapWithoutLookingUpTheSourceWhenTheSourceIsUnchanged() {
        givenExistingMap();

        updateTransportMapByIdUseCase.execute(new UpdateTransportMapByIdCommand(mapId, RENAMED_METADATA, NOW));

        assertThat(captureSavedMap().getMapMetadata()).isEqualTo(RENAMED_METADATA);
        verify(transportMapPort, never()).findBySource(any(DataSource.class));
    }

    @Test
    void shouldThrowMapAlreadyExistsExceptionWithoutSavingWhenSourceBelongsToAnotherMap() {
        givenExistingMap();
        TransportMap anotherMap = TransportMap.createTransportMap(NEW_METADATA, CREATED_AT);
        when(transportMapPort.findBySource(NEW_METADATA.source())).thenReturn(Optional.of(anotherMap));

        assertThatThrownBy(() -> updateTransportMapByIdUseCase.execute(
                new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW)))
                .isInstanceOf(MapAlreadyExistsException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWithoutSavingWhenMapIsNotFound() {
        RuntimeException notFound = new RuntimeException("Transport map not found");
        when(transportMapPort.findById(mapId, TransportMap.class)).thenThrow(notFound);

        assertThatThrownBy(() -> updateTransportMapByIdUseCase.execute(
                new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW)))
                .isSameAs(notFound);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateIllegalArgumentExceptionWithoutSavingWhenNowIsBeforeTheLastUpdate() {
        givenExistingMap();
        Instant beforeLastUpdate = CREATED_AT.minusSeconds(1);

        assertThatThrownBy(() -> updateTransportMapByIdUseCase.execute(
                new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, beforeLastUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWithoutSavingWhenSourceLookupFails() {
        givenExistingMap();
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(NEW_METADATA.source())).thenThrow(lookupFailure);

        assertThatThrownBy(() -> updateTransportMapByIdUseCase.execute(
                new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        givenExistingMap();
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));

        assertThatThrownBy(() -> updateTransportMapByIdUseCase.execute(
                new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW)))
                .isSameAs(persistenceFailure);
    }

    @Test
    void shouldFindTheMapAndLookUpTheSourceBeforeSaving() {
        givenExistingMap();

        updateTransportMapByIdUseCase.execute(new UpdateTransportMapByIdCommand(mapId, NEW_METADATA, NOW));

        InOrder inOrder = inOrder(transportMapPort);
        inOrder.verify(transportMapPort).findById(mapId, TransportMap.class);
        inOrder.verify(transportMapPort).findBySource(NEW_METADATA.source());
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    private void givenExistingMap() {
        when(transportMapPort.findById(mapId, TransportMap.class)).thenReturn(existingMap);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }
}
