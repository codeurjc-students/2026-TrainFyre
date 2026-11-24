package es.codeurjc.students.trainfyre.network.domain;

/**
 * Thrown when no {@link TransportMap} exists for the requested identifier or {@link DataSource}.
 */
public class MapNotFoundException extends RuntimeException {

    public MapNotFoundException(String message) {
        super(message);
    }
}
