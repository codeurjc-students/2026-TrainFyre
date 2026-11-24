package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.UpdateTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.UpdateTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.verifyLookedUp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateTransportMapServiceTest {

    private static final MapMetadata CURRENT_METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata NEW_METADATA = new MapMetadata(
            new DataSource("renfe", "cercanias-valencia"), "Cercanias Valencia", ZoneId.of("Europe/Madrid"), null, null);
    private static final MapMetadata RENAMED_METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid Centro", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private UpdateTransportMapUseCase updateTransportMapUseCase;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        MapSourcePolicy mapSourcePolicy = new MapSourcePolicy(transportMapPort);
        updateTransportMapUseCase = new UpdateTransportMapService(
                transportMapPort, new MapResolver(transportMapPort, mapSourcePolicy), mapSourcePolicy);
        existingMap = TransportMap.createTransportMap(CURRENT_METADATA, CREATED_AT);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldReplaceMetadataAndSaveTheMapFoundByTheRefWhenCommandIsValid(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        Void result = updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW));

        TransportMap savedMap = captureSavedMap();
        assertThat(result).isNull();
        assertThat(savedMap).isSameAs(existingMap);
        assertThat(savedMap.getMapMetadata()).isEqualTo(NEW_METADATA);
        assertThat(savedMap.getLastUpdatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldSaveTheMapWithoutLookingUpTheSourceAgainWhenTheSourceIsUnchanged(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        int lookupsMadeByTheRef = ref instanceof MapRef.BySource ? 1 : 0;

        updateTransportMapUseCase.execute(commandWith(ref, RENAMED_METADATA, NOW));

        assertThat(captureSavedMap().getMapMetadata()).isEqualTo(RENAMED_METADATA);
        verify(transportMapPort, times(lookupsMadeByTheRef)).findBySource(any(DataSource.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldThrowMapNotFoundExceptionWithoutSavingWhenNoMapMatchesTheRef(String scenario, MapRef ref) {
        givenNoMapFound(transportMapPort, ref);

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldThrowMapAlreadyExistsExceptionWithoutSavingWhenNewSourceBelongsToAnotherMap(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        TransportMap anotherMap = TransportMap.createTransportMap(NEW_METADATA, CREATED_AT);
        doReturn(Optional.of(anotherMap)).when(transportMapPort).findBySource(NEW_METADATA.source());

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW)))
                .isInstanceOf(MapAlreadyExistsException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateIllegalArgumentExceptionWithoutSavingWhenNowIsBeforeTheLastUpdate(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        Instant beforeLastUpdate = CREATED_AT.minusSeconds(1);

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, beforeLastUpdate)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWithoutSavingWhenTheLookupFails(String scenario, MapRef ref) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, ref, lookupFailure);

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWithoutSavingWhenNewSourceLookupFails(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        doThrow(lookupFailure).when(transportMapPort).findBySource(NEW_METADATA.source());

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).save(any(TransportMap.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWhenSavingFails(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        RuntimeException persistenceFailure = new RuntimeException("Could not save transport map");
        doThrow(persistenceFailure).when(transportMapPort).save(any(TransportMap.class));

        assertThatThrownBy(() -> updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW)))
                .isSameAs(persistenceFailure);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldFindTheMapAndLookUpTheNewSourceBeforeSaving(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        updateTransportMapUseCase.execute(commandWith(ref, NEW_METADATA, NOW));

        InOrder inOrder = inOrder(transportMapPort);
        verifyLookedUp(inOrder, transportMapPort, ref);
        inOrder.verify(transportMapPort).findBySource(NEW_METADATA.source());
        inOrder.verify(transportMapPort).save(any(TransportMap.class));
    }

    private static UpdateTransportMapCommand commandWith(MapRef ref, MapMetadata metadata, Instant now) {
        return new UpdateTransportMapCommand(ref, metadata, now);
    }

    private TransportMap captureSavedMap() {
        ArgumentCaptor<TransportMap> captor = ArgumentCaptor.forClass(TransportMap.class);
        verify(transportMapPort).save(captor.capture());
        return captor.getValue();
    }
}
