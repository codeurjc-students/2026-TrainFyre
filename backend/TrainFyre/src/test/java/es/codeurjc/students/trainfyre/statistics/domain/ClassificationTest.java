package es.codeurjc.students.trainfyre.statistics.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassificationTest {

    private Severity severity = Severity.MODERATE;
    private Cause cause = Cause.MEDICAL_EMERGENCY;

    @Test
    void shouldRejectNullSeverity(){
        assertThrows(IllegalArgumentException.class, () ->{
           new Classification(null, cause);
        });
    }

    @Test
    void shouldRejectNullCause(){
        assertThrows(IllegalArgumentException.class, ()-> {
            new Classification(severity, null);
        });
    }

}
