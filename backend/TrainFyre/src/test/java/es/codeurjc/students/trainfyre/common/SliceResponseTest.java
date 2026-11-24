package es.codeurjc.students.trainfyre.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SliceResponseTest {

    private static final List<Integer> CONTENT = List.of(1, 2, 3);

    static Stream<Arguments> invalidNullContents() {
        return Stream.of(
                Arguments.of("null list", null),
                Arguments.of("list containing a null element", Arrays.asList(1, null, 3))
        );
    }

    static Stream<Arguments> validSlices() {
        return Stream.of(
                Arguments.of("first slice with next", CONTENT, 0, true),
                Arguments.of("middle slice with next", CONTENT, 4, true),
                Arguments.of("last slice without next", CONTENT, 2, false),
                Arguments.of("empty slice without next", List.<Integer>of(), 0, false),
                Arguments.of("empty slice beyond the last page", List.<Integer>of(), 7, false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNullContents")
    void shouldRejectNullContentOrNullElements(String caseName, List<Integer> content) {
        assertThatThrownBy(() -> new SliceResponse<>(content, 0, false))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "page {0}")
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void shouldRejectNegativePage(int page) {
        assertThatThrownBy(() -> new SliceResponse<>(CONTENT, page, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectHasNextWhenContentIsEmpty() {
        assertThatThrownBy(() -> new SliceResponse<>(List.<Integer>of(), 0, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validSlices")
    void shouldKeepContentPageAndHasNextWhenArgumentsAreValid(String caseName, List<Integer> content, int page, boolean hasNext) {
        SliceResponse<Integer> slice = new SliceResponse<>(content, page, hasNext);

        assertThat(slice.content()).containsExactlyElementsOf(content);
        assertThat(slice.page()).isEqualTo(page);
        assertThat(slice.hasNext()).isEqualTo(hasNext);
    }

    @Test
    void shouldNotBeAffectedByLaterChangesToTheOriginalList() {
        List<Integer> original = new ArrayList<>(CONTENT);
        SliceResponse<Integer> slice = new SliceResponse<>(original, 0, true);

        original.add(4);
        original.remove(0);

        assertThat(slice.content()).containsExactlyElementsOf(CONTENT);
    }

    @Test
    void shouldExposeAnImmutableContent() {
        SliceResponse<Integer> slice = new SliceResponse<>(new ArrayList<>(CONTENT), 0, true);

        assertThatThrownBy(() -> slice.content().add(4))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
