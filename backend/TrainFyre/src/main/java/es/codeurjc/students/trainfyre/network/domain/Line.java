package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;

/**
 * Line of a {@link TransportMap}. Its external id never changes and identifies it within its map. Its
 * {@link LineId} is absent until the line is persisted, so a line created with
 * {@link #createLine(String, LineName, LineType, LineColors)} has no id. The rest of its data can only be
 * changed through the {@link TransportMap} it belongs to.
 * <p>
 * It does not define {@code equals} and {@code hashCode}, so two instances are equal only if they are the
 * same object.
 */
@Entity
@Getter
public final class Line {

    @Identity
    private final LineId id;
    private final String externalId;
    private LineName name;
    private LineType type;
    private LineColors colors;

    private Line(LineId id, String externalId, LineName name, LineType type, LineColors colors) {
        this.id = id;
        this.externalId = externalId;
        this.name = Objects.requireNonNull(name, "name should not be null");
        this.type = Objects.requireNonNull(type, "line type should not be null");
        this.colors = Objects.requireNonNull(colors, "line colors should not be null");
    }

    /**
     * Creates a line that has not been persisted yet, so it has no {@link LineId}. The external id is
     * stripped. If {@code colors} is {@code null}, {@link LineColors#defaultFor(String)} is used.
     *
     * @throws NullPointerException if {@code externalId}, {@code name} or {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank
     */
    public static Line createLine(String externalId, LineName name, LineType type, LineColors colors) {
        String normalizedExternalId = normalizeExternalId(externalId);
        LineColors resolvedColors = colors != null ? colors : LineColors.defaultFor(normalizedExternalId);

        return new Line(null, normalizedExternalId, name, type, resolvedColors);
    }

    /**
     * Rebuilds a persisted line. Unlike {@link #createLine(String, LineName, LineType, LineColors)}, the id
     * and the colors are required. The external id is stripped.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank
     */
    public static Line reconstitute(LineId id, String externalId, LineName name, LineType type, LineColors colors) {
        Objects.requireNonNull(id, "id should not be null");

        return new Line(id, normalizeExternalId(externalId), name, type, colors);
    }

    /**
     * Strips an external id. It is the single definition of a valid external id: {@link TransportMap} uses
     * it to normalize the external ids it searches, and the application layer uses it to normalize the ones
     * it receives, so every layer agrees on what identifies a line.
     *
     * @throws NullPointerException if {@code externalId} is {@code null}
     * @throws IllegalArgumentException if {@code externalId} is empty or blank
     */
    public static String normalizeExternalId(String externalId) {
        Objects.requireNonNull(externalId, "external id should not be null");

        String normalized = externalId.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("external id cannot be blank");
        }
        return normalized;
    }

    /**
     * Applies the components of the changes that are not {@code null}. It is package-private on purpose:
     * only {@link TransportMap} may call it, which {@code LineEncapsulationArchitectureTest} enforces.
     *
     * @throws NullPointerException if {@code lineChanges} is {@code null}
     */
    void update(LineChanges lineChanges) {
        Objects.requireNonNull(lineChanges, "line changes should not be null");

        if (lineChanges.name() != null) {
            name = lineChanges.name();
        }
        if (lineChanges.type() != null) {
            type = lineChanges.type();
        }
        if (lineChanges.colors() != null) {
            colors = lineChanges.colors();
        }
    }
}
