package es.codeurjc.students.trainfyre.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PagedResponseTest {

    @Test
    void shouldRejectNullList(){
        assertThrows(NullPointerException.class, () -> new PagedResponse<Integer>(null, 1, 3,3));
    }

    @Test
    void shouldRejectNegativePage(){
        assertThrows(IllegalArgumentException.class, () -> new PagedResponse<Integer>(List.of(1,2,3), -1, 3,3));
    }

    @Test
    void shouldRejectNegativeSize(){
        assertThrows(IllegalArgumentException.class, () -> new PagedResponse<Integer>(List.of(1,2,3), 0, -1,3));
    }

    @Test
    void shouldRejectNegativeTotalElements(){
        assertThrows(IllegalArgumentException.class, () -> new PagedResponse<Integer>(List.of(1,2,3), 0, 0,-1));
    }

}
