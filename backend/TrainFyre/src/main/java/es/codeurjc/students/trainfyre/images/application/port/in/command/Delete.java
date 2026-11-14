package es.codeurjc.students.trainfyre.images.application.port.in.command;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.jmolecules.architecture.cqrs.Command;

import java.util.Objects;
import java.util.UUID;

@Command
public record Delete(AuthenticatedUser authenticatedUser, UUID uuid) {

    public Delete {
        Objects.requireNonNull(uuid, "uuid should not be null");
        Objects.requireNonNull(authenticatedUser, "user should not be null");
    }

    public void assertCanChangeAvatar() {
        if(!authenticatedUser.isOwner(uuid()) && !authenticatedUser().hasRole(Role.ADMIN)){
            throw new ChangeAvatarDeniedException(authenticatedUser.id());
        }
    }
}
