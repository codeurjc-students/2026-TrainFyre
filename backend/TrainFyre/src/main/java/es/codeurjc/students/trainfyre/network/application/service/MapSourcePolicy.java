package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import es.codeurjc.students.trainfyre.network.domain.MapNotFoundException;
import es.codeurjc.students.trainfyre.network.domain.TransportMap;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Rules about the {@link DataSource} of a transport map: it must be unique across the maps of the system
 * and it must be resolvable to the map that has it.
 */
@Component
@AllArgsConstructor
public class MapSourcePolicy {

    private final TransportMapPort transportMapPort;

    /**
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws MapAlreadyExistsException if a map already has the source
     */
    public void requireUnusedSource(DataSource source) {
        Objects.requireNonNull(source, "source should not be null");
        if (transportMapPort.findBySource(source).isPresent()) {
            throw new MapAlreadyExistsException("a map with source " + source + " already exists");
        }
    }

    /**
     * @return the map that has the source
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws MapNotFoundException if no map has the source
     */
    public TransportMap requireExistingMap(DataSource source) {
        Objects.requireNonNull(source, "source should not be null");
        return transportMapPort.findBySource(source)
                .orElseThrow(() -> new MapNotFoundException("no map with source " + source + " exists"));
    }
}
