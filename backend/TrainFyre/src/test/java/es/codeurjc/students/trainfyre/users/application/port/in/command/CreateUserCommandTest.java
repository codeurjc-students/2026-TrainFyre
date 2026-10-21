package es.codeurjc.students.trainfyre.users.application.port.in.command;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateUserCommandTest {

    @ParameterizedTest(name = "[{index}] username={0}, password={1}")
    @MethodSource("nullAttributes")
    void shouldThrowNullPointerExceptionWhenAnyAttributeIsNull(
            String username,
            String password
    ) {
        assertThrows(
                NullPointerException.class,
                () -> new CreateUserCommand(username, password)
        );
    }

    private static Stream<Arguments> nullAttributes() {
        return Stream.of(
                Arguments.of(null, "valid-password"),
                Arguments.of("pablo", null),
                Arguments.of(null, null)
        );
    }
}
