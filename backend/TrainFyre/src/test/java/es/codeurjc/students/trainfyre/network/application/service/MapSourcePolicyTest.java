package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapSourcePolicyTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private MapSourcePolicy mapSourcePolicy;

    @BeforeEach
    void setUp() {
        mapSourcePolicy = new MapSourcePolicy(transportMapPort);
    }

    @Test
    void shouldLookUpTheSourceAndAcceptItWhenNoMapHasIt() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.empty());

        assertThatCode(() -> mapSourcePolicy.requireUnusedSource(SOURCE)).doesNotThrowAnyException();
        verify(transportMapPort).findBySource(SOURCE);
    }

    @Test
    void shouldThrowMapAlreadyExistsExceptionWhenAMapHasTheSource() {
        when(transportMapPort.findBySource(SOURCE))
                .thenReturn(Optional.of(TransportMap.createTransportMap(METADATA, NOW)));

        assertThatThrownBy(() -> mapSourcePolicy.requireUnusedSource(SOURCE))
                .isInstanceOf(MapAlreadyExistsException.class);
    }

    @Test
    void shouldPropagateExceptionWhenSourceLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> mapSourcePolicy.requireUnusedSource(SOURCE)).isSameAs(lookupFailure);
    }

    @Test
    void shouldThrowNullPointerExceptionWithoutLookingUpWhenSourceIsNull() {
        assertThatThrownBy(() -> mapSourcePolicy.requireUnusedSource(null))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(transportMapPort);
    }

    @Test
    void shouldReturnTheMapFoundBySourceWhenAMapHasTheSource() {
        TransportMap existingMap = TransportMap.createTransportMap(METADATA, NOW);
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.of(existingMap));

        assertThat(mapSourcePolicy.requireExistingMap(SOURCE)).isSameAs(existingMap);
    }

    @Test
    void shouldThrowMapNotFoundExceptionWhenNoMapHasTheSourceToRequire() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapSourcePolicy.requireExistingMap(SOURCE))
                .isInstanceOf(MapNotFoundException.class);
    }

    @Test
    void shouldPropagateExceptionWhenExistingMapLookupFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> mapSourcePolicy.requireExistingMap(SOURCE)).isSameAs(lookupFailure);
    }

    @Test
    void shouldThrowNullPointerExceptionWithoutLookingUpWhenRequiredSourceIsNull() {
        assertThatThrownBy(() -> mapSourcePolicy.requireExistingMap(null))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(transportMapPort);
    }
}
