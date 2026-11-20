package es.codeurjc.students.trainfyre.network.domain;

/**
 * Thrown when a {@link TransportMap} would have the same {@link DataSource} as an existing one.
 */
public class MapAlreadyExistsException extends RuntimeException {

    public MapAlreadyExistsException(String message) {
        super(message);
    }
}
