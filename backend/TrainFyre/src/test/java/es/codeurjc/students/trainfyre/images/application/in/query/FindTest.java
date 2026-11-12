package es.codeurjc.students.trainfyre.images.application.in.query;

import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FindTest {

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(
                NullPointerException.class,
                creation
        );
    }

    static Stream<Executable> invalidNullArguments() {
        UUID uuid = UUID.randomUUID();
        es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser user =
                new es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser(uuid, Set.of());
        return Stream.of(
                () -> new Find(null, uuid),
                () -> new Find(user, null)
        );
    }
}
