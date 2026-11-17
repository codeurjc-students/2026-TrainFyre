package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Changes to apply to a {@link Line}. A {@code null} component means "no change".
 */
@ValueObject
public record LineChanges(LineName name, LineType type, LineColors colors) {
}
