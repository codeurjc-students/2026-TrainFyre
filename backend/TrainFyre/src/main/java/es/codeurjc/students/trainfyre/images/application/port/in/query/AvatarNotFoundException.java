package es.codeurjc.students.trainfyre.images.application.port.in.query;

import java.util.UUID;

public class AvatarNotFoundException extends RuntimeException {
    public AvatarNotFoundException(UUID userId) {
        super("Avatar not found for user id:" + userId);
    }
}
