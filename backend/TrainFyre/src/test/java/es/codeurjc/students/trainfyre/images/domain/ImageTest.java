package es.codeurjc.students.trainfyre.images.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageTest {

    private static final UUID IMAGE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final InputStream CONTENT =
            new ByteArrayInputStream(new byte[]{1, 2, 3});
    private static final long CONTENT_LENGTH = 3L;
    private static final String CONTENT_TYPE = "image/png";

    @ParameterizedTest
    @MethodSource("invalidNullArguments")
    void shouldRejectNullArguments(Executable creation) {
        assertThrows(NullPointerException.class, creation);
    }

    static Stream<Executable> invalidNullArguments() {
        return Stream.of(
                () -> new Image((UUID) null, CONTENT, CONTENT_LENGTH, CONTENT_TYPE),
                () -> new Image(IMAGE_ID, (InputStream) null, CONTENT_LENGTH, CONTENT_TYPE),
                () -> new Image(IMAGE_ID, CONTENT, CONTENT_LENGTH, (String) null)
        );
    }

    @ParameterizedTest(name = "[{index}] contentLength={0}")
    @MethodSource("invalidContentLengths")
    void shouldRejectNegativeContentLength(long contentLength) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Image(IMAGE_ID, CONTENT, contentLength, CONTENT_TYPE)
        );
    }

    private static Stream<Arguments> invalidContentLengths() {
        return Stream.of(
                Arguments.of(-1L),
                Arguments.of(Long.MIN_VALUE)
        );
    }

    @Test
    void shouldCreateImageWithValidAttributes() {
        Image image = new Image(IMAGE_ID, CONTENT, CONTENT_LENGTH, CONTENT_TYPE);

        assertThat(image.uuid()).isEqualTo(IMAGE_ID);
        assertThat(image.content()).isEqualTo(CONTENT);
        assertThat(image.contentLength()).isEqualTo(CONTENT_LENGTH);
        assertThat(image.contentType()).isEqualTo(CONTENT_TYPE);
    }
}
