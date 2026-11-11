package es.codeurjc.students.trainfyre.images.application.port.in.command;

import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record Delete(UUID uuid) {

    public Delete {
        Objects.requireNonNull(uuid, "uuid should not be null");
    }
}
