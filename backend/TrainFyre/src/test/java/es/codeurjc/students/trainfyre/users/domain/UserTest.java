package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("nullCredentials")
    void createUserShouldThrowNullPointerExceptionWhenCredentialsAreNull(
            String scenario,
            String username,
            String password
    ) {
        assertThrows(NullPointerException.class, () -> User.createUser(username, password));
    }

    private static Stream<Arguments> nullCredentials() {
        return Stream.of(
                Arguments.of("username null", null, "valid-password"),
                Arguments.of("password null", "pablo", null),
                Arguments.of("ambos null", null, null)
        );
    }

}
