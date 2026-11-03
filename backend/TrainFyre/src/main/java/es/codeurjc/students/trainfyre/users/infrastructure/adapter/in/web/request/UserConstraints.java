package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request;

final class UserConstraints {

    static final String USERNAME_PATTERN = "^[A-Za-z0-9._-]+$";
    static final int USERNAME_MIN_LENGTH = 3;
    static final int USERNAME_MAX_LENGTH = 32;
    static final int PASSWORD_MIN_LENGTH = 12;
    static final int PASSWORD_MAX_LENGTH = 128;

    private UserConstraints() {
    }
}
