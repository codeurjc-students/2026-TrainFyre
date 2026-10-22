package es.codeurjc.students.trainfyre.users.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDetailsTest {

    @ParameterizedTest
    @MethodSource("nullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(
            String username,
            List<AlertSubscription> alertSubscriptions
    ) {
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> new UserDetails(username, alertSubscriptions)
        );
    }

    private static Stream<Arguments> nullArguments() {
        return Stream.of(
                Arguments.of(null, List.<AlertSubscription>of()),
                Arguments.of("pablo", null),
                Arguments.of(null, null)
        );
    }
}
