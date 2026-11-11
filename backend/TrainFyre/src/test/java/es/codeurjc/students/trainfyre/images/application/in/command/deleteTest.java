package es.codeurjc.students.trainfyre.images.application.in.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class deleteTest {

    @ParameterizedTest(name = "[{index}] uuid={0}")
    @MethodSource("nullAttributes")
    void shouldThrowNullPointerExceptionWhenUuidIsNull(UUID uuid) {
        assertThrows(
                NullPointerException.class,
                () -> new delete(uuid)
        );
    }

    private static Stream<Arguments> nullAttributes() {
        return Stream.of(
                Arguments.of((UUID) null)
        );
    }
}
