package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DescriptionTest {

    private String name = "Train failure";
    private String summary = "The train's engine has broken down and won't start.";

    @Test
    void shouldRejectNullName(){
        assertThrows(IllegalArgumentException.class, () -> {
           new Description(null, summary);
        });
    }

    @Test
    void shouldRejectNullSummary(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Description(name, null);
        });
    }

    @Test
    void shouldRejectBlankName(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Description("\n\t", summary);
        });
    }
}
