package es.codeurjc.students.trainfyre.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableTest {



    @Test
    void shouldRejectNegativePage(){
        assertThrows(IllegalArgumentException.class, () -> new Pageable(-1, 5));
    }

}
