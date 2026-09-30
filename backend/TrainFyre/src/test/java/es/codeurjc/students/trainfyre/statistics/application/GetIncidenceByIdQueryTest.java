package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GetIncidenceByIdQueryTest {


    @Test
    void shouldRejectNullId(){
        assertThrows(NullPointerException.class, () ->
            new GetIncidenceByIdQuery(null)
        );
    }
}
