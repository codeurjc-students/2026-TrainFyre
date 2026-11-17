package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web.request;

import es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder;
import es.codeurjc.students.trainfyre.statistics.domain.AffectedNetwork;
import es.codeurjc.students.trainfyre.statistics.domain.Classification;
import es.codeurjc.students.trainfyre.statistics.domain.Description;
import es.codeurjc.students.trainfyre.statistics.domain.Occurrence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateIncidenceRequestTest {

    private static final AffectedNetwork AFFECTED_NETWORK = IncidenceTestBuilder.generateDefaultAffectedNetwork();
    private static final Occurrence OCCURRENCE = IncidenceTestBuilder.generateDefaultOccurrence();
    private static final Description DESCRIPTION = IncidenceTestBuilder.generateDefaultDescription();
    private static final Classification CLASSIFICATION = IncidenceTestBuilder.generateDefaultClassification();

    @Test
    void shouldRejectWhenAllArgumentsAreNull() {
        assertThatThrownBy(() -> new UpdateIncidenceRequest(null, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestsWithAtLeastOneArgument")
    void shouldAcceptWhenAtLeastOneArgumentIsNotNull(String name, AffectedNetwork affectedNetwork, Occurrence occurrence,
                                                      Description description, Classification classification) {
        assertThatCode(() -> new UpdateIncidenceRequest(affectedNetwork, occurrence, description, classification))
                .doesNotThrowAnyException();
    }

    private static Stream<Arguments> requestsWithAtLeastOneArgument() {
        return Stream.of(
                Arguments.of("only affected network", AFFECTED_NETWORK, null, null, null),
                Arguments.of("only occurrence", null, OCCURRENCE, null, null),
                Arguments.of("only description", null, null, DESCRIPTION, null),
                Arguments.of("only classification", null, null, null, CLASSIFICATION),
                Arguments.of("all arguments", AFFECTED_NETWORK, OCCURRENCE, DESCRIPTION, CLASSIFICATION)
        );
    }
}
