package es.codeurjc.students.trainfyre.network.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapIdTest {

    private static final UUID VALUE = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final int RANDOM_UUID_VERSION = 4;
    private static final int GENERATED_IDS = 100;

    @Test
    void shouldExposeValueWhenValueIsNotNull() {
        assertThat(new MapId(VALUE).value()).isEqualTo(VALUE);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> new MapId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldGenerateRandomVersion4UuidWhenGenerateIsCalled() {
        assertThat(MapId.generate().value().version()).isEqualTo(RANDOM_UUID_VERSION);
    }

    @Test
    void shouldGenerateDifferentIdsWhenGenerateIsCalledRepeatedly() {
        Set<MapId> ids = Stream.generate(MapId::generate)
                .limit(GENERATED_IDS)
                .collect(Collectors.toSet());

        assertThat(ids).hasSize(GENERATED_IDS);
    }
}
