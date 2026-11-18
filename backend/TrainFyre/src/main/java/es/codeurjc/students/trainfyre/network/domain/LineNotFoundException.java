package es.codeurjc.students.trainfyre.network.domain;

/**
 * Thrown when a {@link TransportMap} does not contain the requested line,
 * either by {@link LineId} or by external id.
 */
public class LineNotFoundException extends RuntimeException {

    public LineNotFoundException(String message) {
        super(message);
    }
}
