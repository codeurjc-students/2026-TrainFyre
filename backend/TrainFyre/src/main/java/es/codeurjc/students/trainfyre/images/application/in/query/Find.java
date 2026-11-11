package es.codeurjc.students.trainfyre.images.application.in.query;

import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record Find(UUID uuid) {

    public Find {
        Objects.requireNonNull(uuid, "uuid should not be null");
    }
}
