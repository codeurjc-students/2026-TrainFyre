package es.codeurjc.students.trainfyre.images.application.in.command;

import es.codeurjc.students.trainfyre.images.domain.Image;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record Upload(Image image) {

    public Upload {
        Objects.requireNonNull(image, "image should not be null");
    }
}
