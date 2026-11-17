package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;

@Entity
@Getter
public class Line {

    @Identity
    private final LineId id;
    private final String externalId;
    private LineName name;
    private LineType type;
    private LineColors colors;

    private Line(LineId id, String externalId, LineName name, LineType type, LineColors colors) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.type = type;
        this.colors = colors;
    }

    public static Line createLine(String externalId, LineName name, LineType type, LineColors colors) {

        Objects.requireNonNull(externalId);

        String normalizedExternalId = externalId.strip();
        if(normalizedExternalId.isEmpty()) throw new IllegalArgumentException();

        Objects.requireNonNull(name);
        Objects.requireNonNull(type);

        if(colors == null) {
            colors = LineColors.defaultFor(normalizedExternalId);
        }

        return new Line(null, normalizedExternalId, name, type, colors);

    }

    public static Line reconstitute(LineId id, String externalId, LineName name, LineType type, LineColors colors) {

        Objects.requireNonNull(id, "id should not be null");
        Objects.requireNonNull(externalId, "external id should not be null");

        if(externalId.isBlank()) throw new IllegalArgumentException("external id cannot be blank");

        Objects.requireNonNull(name, "name should not be null");
        Objects.requireNonNull(type, "line type should not be null");
        Objects.requireNonNull(colors, "line colors should not be null");

        return new Line(id, externalId.trim(), name, type, colors);

    }

}
