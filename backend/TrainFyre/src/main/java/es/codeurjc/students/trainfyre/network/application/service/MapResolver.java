package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.ref.MapRef;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Resolves a {@link MapRef} to the transport map it identifies.
 */
@Component
@AllArgsConstructor
public class MapResolver {

    private final TransportMapPort transportMapPort;
    private final MapSourcePolicy mapSourcePolicy;

    /**
     * Failures of the lookup by id are those of {@link TransportMapPort#findById}, which is expected to
     * throw {@link MapNotFoundException} when the map does not exist.
     *
     * @return the map identified by the reference
     * @throws NullPointerException if {@code ref} is {@code null}
     * @throws MapNotFoundException if no map matches the reference
     */
    public TransportMap require(MapRef ref) {
        Objects.requireNonNull(ref, "map ref should not be null");

        return switch (ref) {
            case MapRef.ById(UUID id) -> transportMapPort.findById(id, TransportMap.class);
            case MapRef.BySource(DataSource source) -> mapSourcePolicy.requireExistingMap(source);
        };
    }
}
