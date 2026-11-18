package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;

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

    public static Line createLine(String externalId, LineName name, LineType type, LineColors colors) {
        String normalizedExternalId = normalizeExternalId(externalId);
        LineColors resolvedColors = colors != null ? colors : LineColors.defaultFor(normalizedExternalId);

        return new Line(null, normalizedExternalId, name, type, resolvedColors);
    }

    public static Line reconstitute(LineId id, String externalId, LineName name, LineType type, LineColors colors) {
        Objects.requireNonNull(id, "id should not be null");

        return new Line(id, normalizeExternalId(externalId), name, type, colors);
    }

    private static String normalizeExternalId(String externalId) {
        Objects.requireNonNull(externalId, "external id should not be null");

        String normalized = externalId.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("external id cannot be blank");
        }
        return normalized;
    }

    void update(LineChanges lineChanges) {
        Objects.requireNonNull(lineChanges, "line changes should not be null");

        if (lineChanges.name() != null) name = lineChanges.name();
        if (lineChanges.type() != null) type = lineChanges.type();
        if (lineChanges.colors() != null) colors = lineChanges.colors();
    }

}
