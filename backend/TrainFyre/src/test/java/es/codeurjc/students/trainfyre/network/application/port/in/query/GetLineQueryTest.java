package es.codeurjc.students.trainfyre.network.application.port.in.query;

import es.codeurjc.students.trainfyre.network.application.port.in.LineRef;
import es.codeurjc.students.trainfyre.network.application.port.in.MapRef;
import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetLineQueryTest {

    private static final MapRef MAP = new MapRef.BySource(new DataSource("renfe", "cercanias-madrid"));
    private static final LineRef LINE = new LineRef.ByExternalId("C-1");

    static Stream<Arguments> invalidNullArguments() {
        return Stream.of(
                Arguments.of("null map", null, LINE),
                Arguments.of("null line", MAP, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullArguments")
    void shouldThrowNullPointerExceptionWhenAnyArgumentIsNull(String scenario, MapRef map, LineRef line) {
        assertThatThrownBy(() -> new GetLineQuery(map, line)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenArgumentsWhenBothAreProvided() {
        GetLineQuery query = new GetLineQuery(MAP, LINE);

        assertThat(query.map()).isEqualTo(MAP);
        assertThat(query.line()).isEqualTo(LINE);
    }
}
