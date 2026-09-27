package es.codeurjc.students.trainfyre.statistics.domain;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class AffectedNetworkTest {

    private Long mapId = 1L;
    private List<Long> lineIds = Arrays.asList(1L, 2L, 3L);

    @Test
    void shouldRejectNullMap(){

        assertThrows(IllegalArgumentException.class, () -> {
            new AffectedNetwork(null, lineIds);
        });

    }

}
