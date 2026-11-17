package es.codeurjc.students.trainfyre.network.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;

import java.util.Objects;

@Entity
@Getter
public class Line {

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

        return new Line(null, normalizedExternalId, name, type, colors);

    }
}
