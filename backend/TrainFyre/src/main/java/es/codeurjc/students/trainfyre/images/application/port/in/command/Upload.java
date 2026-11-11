package es.codeurjc.students.trainfyre.images.application.port.in.command;

import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;

@Command
public record Upload(AuthenticatedUser authenticatedUser, Image image) {

    public Upload {
        Objects.requireNonNull(image, "image should not be null");
        Objects.requireNonNull(authenticatedUser, "user should not be null");
    }

    public void assertCanChangeAvatar() {
        //TODO
    }
}
