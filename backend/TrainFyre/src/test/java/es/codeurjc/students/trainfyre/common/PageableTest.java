package es.codeurjc.students.trainfyre.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableTest {


    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of(null, 10),
                Arguments.of(0, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(int page, int size){
        assertThrows(NullPointerException.class, () -> new Pageable(page, size));
    }

}
