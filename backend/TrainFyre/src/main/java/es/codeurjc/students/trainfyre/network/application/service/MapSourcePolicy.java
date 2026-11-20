package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Keeps the {@link DataSource} of a transport map unique across the maps of the system.
 */
@Component
@AllArgsConstructor
public class MapSourcePolicy {

    private final TransportMapPort transportMapPort;

    /**
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws es.codeurjc.students.trainfyre.network.domain.MapAlreadyExistsException if a map already has the source
     */
    public void requireUnusedSource(DataSource source) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
