package es.codeurjc.students.trainfyre.statistics.application;

import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteIncidenceCommandTest {


    @Test
    void shouldRejectNullId(){
        assertThrows(NullPointerException.class,
                () -> new DeleteIncidenceCommand(null)
        );
    }
}
