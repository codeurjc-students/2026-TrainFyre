package es.codeurjc.students.trainfyre.images.application.port.in.command;

import java.util.UUID;

public class ChangeAvatarDeniedException extends RuntimeException {
    public ChangeAvatarDeniedException(UUID requesterId) {
        super("In order to change avatar, id must be the same as user id:" + requesterId + " or user must have admin role");
    }
}
