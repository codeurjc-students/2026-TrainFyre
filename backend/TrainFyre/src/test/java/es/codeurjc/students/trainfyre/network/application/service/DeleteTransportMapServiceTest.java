package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.MapMetadata;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.SOURCE;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenLookupFails;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.givenNoMapFound;
import static es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures.verifyLookedUp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransportMapServiceTest {

    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant CREATED_AT = Instant.parse("2025-12-31T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private DeleteTransportMapUseCase deleteTransportMapUseCase;
    private TransportMap existingMap;
    private UUID mapId;

    @BeforeEach
    void setUp() {
        deleteTransportMapUseCase = new DeleteTransportMapService(
                transportMapPort, new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort)));
        existingMap = TransportMap.createTransportMap(METADATA, CREATED_AT);
        mapId = existingMap.getId().value();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldDeleteTheMapFoundByTheRefWhenCommandIsValid(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        Void result = deleteTransportMapUseCase.execute(new DeleteTransportMapCommand(ref));

        assertThat(result).isNull();
        verify(transportMapPort).delete(mapId);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldThrowMapNotFoundExceptionWithoutDeletingWhenNoMapMatchesTheRef(String scenario, MapRef ref) {
        givenNoMapFound(transportMapPort, ref);

        assertThatThrownBy(() -> deleteTransportMapUseCase.execute(new DeleteTransportMapCommand(ref)))
                .isInstanceOf(MapNotFoundException.class);
        verify(transportMapPort, never()).delete(any(UUID.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWithoutDeletingWhenTheLookupFails(String scenario, MapRef ref) {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        givenLookupFails(transportMapPort, ref, lookupFailure);

        assertThatThrownBy(() -> deleteTransportMapUseCase.execute(new DeleteTransportMapCommand(ref)))
                .isSameAs(lookupFailure);
        verify(transportMapPort, never()).delete(any(UUID.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldPropagateExceptionWhenDeletingFails(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);
        RuntimeException persistenceFailure = new RuntimeException("Could not delete transport map");
        when(transportMapPort.delete(mapId)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> deleteTransportMapUseCase.execute(new DeleteTransportMapCommand(ref)))
                .isSameAs(persistenceFailure);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("es.codeurjc.students.trainfyre.network.application.service.MapRefFixtures#mapRefs")
    void shouldFindTheMapBeforeDeletingIt(String scenario, MapRef ref) {
        givenMapFound(transportMapPort, ref, existingMap);

        deleteTransportMapUseCase.execute(new DeleteTransportMapCommand(ref));

        InOrder inOrder = inOrder(transportMapPort);
        verifyLookedUp(inOrder, transportMapPort, ref);
        inOrder.verify(transportMapPort).delete(mapId);
    }
}
