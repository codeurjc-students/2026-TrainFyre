package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapBySourceUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapBySourceCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTransportMapBySourceServiceTest {

    private static final DataSource CURRENT_SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata CURRENT_METADATA = new MapMetadata(
            CURRENT_SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata NEW_METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-valencia"), "Cercanias Valencia", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata RENAMED_METADATA = new MapMetadata(
            CURRENT_SOURCE, "Cercanias Madrid Centro", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private UpdateTransportMapBySourceUseCase updateTransportMapBySourceUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        updateTransportMapBySourceUseCase = new UpdateTransportMapBySourceService(transportMapPort);
        existingMap = TransportMap.createTransportMap(CURRENT_METADATA, CREATED_AT);
    }

    @Test
    void shouldReplaceMetadataAndSaveTheMapFoundBySourceWhenCommandIsValid() {
        givenExistingMap();

        Void result = updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW));

        TransportMap savedMap = captureSavedMap();
        assertThat(result).isNull();
        assertThat(savedMap).isSameAs(existingMap);
        assertThat(savedMap.getMapMetadata()).isEqualTo(NEW_METADATA);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldSaveTheMapWithoutLookingUpTheSourceAgainWhenTheSourceIsUnchanged() {
        givenExistingMap();

        updateTransportMapBySourceUseCase.execute(commandWith(RENAMED_METADATA, NOW));

        assertThat(captureSavedMap().getMapMetadata()).isEqualTo(RENAMED_METADATA);
        verify(transportMapPort, times(1)).findBySource(any(DataSource.class));
    }

    @Test
    void shouldThrowMapNotFoundExceptionWithoutSavingWhenNoMapHasTheSource() {
        when(transportMapPort.findBySource(CURRENT_SOURCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldThrowMapAlreadyExistsExceptionWithoutSavingWhenNewSourceBelongsToAnotherMap() {
        givenExistingMap();
        TransportMap anotherMap = TransportMap.createTransportMap(NEW_METADATA, CREATED_AT);
        when(transportMapPort.findBySource(NEW_METADATA.source())).thenReturn(Optional.of(anotherMap));

        assertThatThrownBy(() -> updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW)))
                .isInstanceOf(MapAlreadyExistsException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateIllegalArgumentExceptionWithoutSavingWhenNowIsBeforeTheLastUpdate() {
        givenExistingMap();
        Instant beforeLastUpdate = CREATED_AT.minusSeconds(1);

        assertThatThrownBy(() -> updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, beforeLastUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWithoutSavingWhenSourceLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(CURRENT_SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @Test
    void shouldPropagateExceptionWhenSavingFails() {
        givenExistingMap();
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));

        assertThatThrownBy(() -> updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW)))
                .isSameAs(persistenceFailure);
    }

    @Test
    void shouldFindTheMapAndLookUpTheNewSourceBeforeSaving() {
        givenExistingMap();

        updateTransportMapBySourceUseCase.execute(commandWith(NEW_METADATA, NOW));

        InOrder inOrder = inOrder(transportMapPort);
        inOrder.verify(transportMapPort).findBySource(CURRENT_SOURCE);
        inOrder.verify(transportMapPort).findBySource(NEW_METADATA.source());
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    private void givenExistingMap() {
        when(transportMapPort.findBySource(CURRENT_SOURCE)).thenReturn(Optional.of(existingMap));
    }

    private static UpdateTransportMapBySourceCommand commandWith(MapMetadata metadata, Instant now) {
        return new UpdateTransportMapBySourceCommand(CURRENT_SOURCE, metadata, now);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }
}
