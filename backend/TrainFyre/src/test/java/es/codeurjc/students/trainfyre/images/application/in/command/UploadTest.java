package es.codeurjc.students.trainfyre.images.application.in.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadTest {

    private static final UUID IMAGE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final InputStream CONTENT =
            new ByteArrayInputStream(new byte[]{1, 2, 3});

    @ParameterizedTest(name = "[{index}] uuid={0}, content={1}, contentType={3}")
    @MethodSource("nullAttributes")
    void shouldThrowNullPointerExceptionWhenAnyReferenceAttributeIsNull(
            UUID uuid,
            InputStream content,
            long contentLength,
            String contentType
    ) {
        assertThrows(
                NullPointerException.class,
                () -> new Upload(uuid, content, contentLength, contentType)
        );
    }

    @ParameterizedTest(name = "[{index}] contentLength={0}")
    @MethodSource("invalidContentLengths")
    void shouldThrowIllegalArgumentExceptionWhenContentLengthIsNegative(long contentLength) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Upload(IMAGE_ID, CONTENT, contentLength, "image/png")
        );
    }

    private static Stream<Arguments> nullAttributes() {
        return Stream.of(
                Arguments.of(null, CONTENT, 3L, "image/png"),
                Arguments.of(IMAGE_ID, null, 3L, "image/png"),
                Arguments.of(IMAGE_ID, CONTENT, 3L, null)
        );
    }

    private static Stream<Arguments> invalidContentLengths() {
        return Stream.of(
                Arguments.of(-1L),
                Arguments.of(Long.MIN_VALUE)
        );
    }
}
