package es.codeurjc.students.trainfyre.images.application.port.in.query;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.jmolecules.architecture.cqrs.QueryModel;

import java.util.Objects;
import java.util.UUID;

@QueryModel
public record Find(AuthenticatedUser authenticatedUser, UUID uuid) {

    public Find {
        Objects.requireNonNull(authenticatedUser, "authenticatedUser should not be null");
        Objects.requireNonNull(uuid, "uuid should not be null");
    }

    public void assertCanFindAvatar() {
        if(!authenticatedUser.isOwner(uuid()) && !authenticatedUser().hasRole(Role.ADMIN)){
            throw new FindAvatarDeniedException(authenticatedUser.id());
        }
    }
}
