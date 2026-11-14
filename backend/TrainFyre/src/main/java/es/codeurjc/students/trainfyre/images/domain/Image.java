package es.codeurjc.students.trainfyre.images.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record Image(UUID uuid, InputStream content, long contentLength, String contentType) {

    public Image {
        Objects.requireNonNull(uuid, "uuid should not be null");
        Objects.requireNonNull(content, "content should not be null");
        Objects.requireNonNull(contentType, "contentType should not be null");
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength should not be negative");
        }
    }
}
