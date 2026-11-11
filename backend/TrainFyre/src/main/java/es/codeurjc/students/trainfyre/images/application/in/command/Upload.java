package es.codeurjc.students.trainfyre.images.application.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Command
public record Upload(UUID uuid, InputStream content, long contentLength, String contentType) {

    public Upload {
        Objects.requireNonNull(uuid, "uuid should not be null");
        Objects.requireNonNull(content, "content should not be null");
        Objects.requireNonNull(contentType, "contentType should not be null");
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength should not be negative");
        }
    }
}
