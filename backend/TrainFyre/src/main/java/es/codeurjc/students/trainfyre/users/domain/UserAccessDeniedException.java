package es.codeurjc.students.trainfyre.users.domain;

import java.util.UUID;

public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException(UUID requesterId, UUID targetUserId) {
        super("User " + requesterId + " is not allowed to access user " + targetUserId);
    }
}
