package es.codeurjc.students.trainfyre.images.application.port.in.query;

import java.util.UUID;

public class FindAvatarDeniedException extends RuntimeException {
    public FindAvatarDeniedException(UUID requesterId) {
        super("In order to find avatar, id must be the same as user id:" + requesterId + " or user must have admin role");
    }
}
