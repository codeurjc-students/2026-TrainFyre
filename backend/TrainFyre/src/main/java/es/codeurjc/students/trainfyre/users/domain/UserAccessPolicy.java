package es.codeurjc.students.trainfyre.users.domain;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;

import java.util.Objects;
import java.util.UUID;

public final class UserAccessPolicy {

    private UserAccessPolicy() {
    }

    public static void assertCanManage(AuthenticatedUser requester, UUID targetUserId) {
        Objects.requireNonNull(requester, "requester should not be null");
        Objects.requireNonNull(targetUserId, "targetUserId should not be null");

        if (!requester.isOwner(targetUserId)) {
            throw new UserAccessDeniedException(requester.id(), targetUserId);
        }
    }
}
