package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

/**
 * Shared by the service tests that receive a {@link MapRef}: both ways of referencing a map and the stubbing
 * of the port each one resolves through.
 */
final class MapRefFixtures {

    static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");
    static final MapRef BY_ID = new MapRef.ById(UUID.fromString("b3f1c2d4-5e6a-4b7c-8d9e-0a1b2c3d4e5f"));
    static final MapRef BY_SOURCE = new MapRef.BySource(SOURCE);

    private MapRefFixtures() {
    }

    static Stream<Arguments> mapRefs() {
        return Stream.of(Arguments.of("by id", BY_ID), Arguments.of("by source", BY_SOURCE));
    }

    static void givenMapFound(TransportMapPort port, MapRef ref, TransportMap map) {
        switch (ref) {
            case MapRef.ById(UUID id) -> when(port.findById(id, TransportMap.class)).thenReturn(map);
            case MapRef.BySource(DataSource source) -> when(port.findBySource(source)).thenReturn(Optional.of(map));
        }
    }

    static void givenNoMapFound(TransportMapPort port, MapRef ref) {
        switch (ref) {
            case MapRef.ById(UUID id) -> when(port.findById(id, TransportMap.class))
                    .thenThrow(new MapNotFoundException("no map with id " + id + " exists"));
            case MapRef.BySource(DataSource source) -> when(port.findBySource(source)).thenReturn(Optional.empty());
        }
    }

    static void givenLookupFails(TransportMapPort port, MapRef ref, RuntimeException failure) {
        switch (ref) {
            case MapRef.ById(UUID id) -> when(port.findById(id, TransportMap.class)).thenThrow(failure);
            case MapRef.BySource(DataSource source) -> when(port.findBySource(source)).thenThrow(failure);
        }
    }

    static void verifyLookedUp(InOrder inOrder, TransportMapPort port, MapRef ref) {
        switch (ref) {
            case MapRef.ById(UUID id) -> inOrder.verify(port).findById(id, TransportMap.class);
            case MapRef.BySource(DataSource source) -> inOrder.verify(port).findBySource(source);
        }
    }
}
