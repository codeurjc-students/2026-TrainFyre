package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Keeps the {@link DataSource} of a transport map unique across the maps of the system.
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
}
