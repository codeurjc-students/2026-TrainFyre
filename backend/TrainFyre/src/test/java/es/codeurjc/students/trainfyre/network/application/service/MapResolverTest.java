package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapResolverTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    private static final MapMetadata METADATA = new MapMetadata(
            SOURCE, "Cercanias Madrid", ZoneId.of("Europe/Madrid"), null, null);
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private TransportMapPort transportMapPort;
    private MapResolver mapResolver;
    private TransportMap existingMap;

    @BeforeEach
    void setUp() {
        mapResolver = new MapResolver(transportMapPort, new MapSourcePolicy(transportMapPort));
        existingMap = TransportMap.createTransportMap(METADATA, NOW);
    }

    @Test
    void shouldReturnTheMapFoundByIdWhenTheRefIsById() {
        when(transportMapPort.findById(existingMap.getId().value(), TransportMap.class)).thenReturn(existingMap);

        TransportMap result = mapResolver.require(new MapRef.ById(existingMap.getId().value()));

        assertThat(result).isSameAs(existingMap);
    }

    @Test
    void shouldPropagateExceptionWhenTheLookupByIdFails() {
        MapNotFoundException notFound = new MapNotFoundException("no map exists");
        when(transportMapPort.findById(existingMap.getId().value(), TransportMap.class)).thenThrow(notFound);

        assertThatThrownBy(() -> mapResolver.require(new MapRef.ById(existingMap.getId().value())))
                .isSameAs(notFound);
    }

    @Test
    void shouldReturnTheMapFoundBySourceWhenTheRefIsBySource() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.of(existingMap));

        TransportMap result = mapResolver.require(new MapRef.BySource(SOURCE));

        assertThat(result).isSameAs(existingMap);
    }

    @Test
    void shouldThrowMapNotFoundExceptionWhenNoMapHasTheSourceOfTheRef() {
        when(transportMapPort.findBySource(SOURCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapResolver.require(new MapRef.BySource(SOURCE)))
                .isInstanceOf(MapNotFoundException.class);
    }

    @Test
    void shouldPropagateExceptionWhenTheLookupBySourceFails() {
        RuntimeException lookupFailure = new RuntimeException("Could not look up transport map");
        when(transportMapPort.findBySource(SOURCE)).thenThrow(lookupFailure);

        assertThatThrownBy(() -> mapResolver.require(new MapRef.BySource(SOURCE))).isSameAs(lookupFailure);
    }

    @Test
    void shouldThrowNullPointerExceptionWithoutLookingUpWhenTheRefIsNull() {
        assertThatThrownBy(() -> mapResolver.require(null)).isInstanceOf(NullPointerException.class);
        verifyNoInteractions(transportMapPort);
    }
}
