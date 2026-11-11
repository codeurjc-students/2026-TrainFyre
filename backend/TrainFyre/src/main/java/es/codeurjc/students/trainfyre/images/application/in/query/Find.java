package es.codeurjc.students.trainfyre.images.application.in.query;

import java.util.Objects;
import java.util.UUID;

public record Find(UUID uuid) {

    public Find {
        Objects.requireNonNull(uuid, "uuid should not be null");
    }
}
