package es.codeurjc.students.trainfyre.network.domain;

/**
 * Thrown when a {@link TransportMap} would contain two lines with the same identity,
 * either the same external id or the same {@link LineId}.
 */
public class DuplicateLineException extends RuntimeException {

    public DuplicateLineException(String message) {
        super(message);
    }
}
