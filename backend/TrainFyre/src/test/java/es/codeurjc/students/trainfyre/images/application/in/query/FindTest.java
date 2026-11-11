package es.codeurjc.students.trainfyre.images.application.in.query;

import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FindTest {

    @ParameterizedTest(name = "[{index}] uuid={0}")
    @MethodSource("nullAttributes")
    void shouldThrowNullPointerExceptionWhenUuidIsNull(UUID uuid) {
        assertThrows(
                NullPointerException.class,
                () -> new Find(uuid)
        );
    }

    private static Stream<Arguments> nullAttributes() {
        return Stream.of(
                Arguments.of((UUID) null)
        );
    }
}
