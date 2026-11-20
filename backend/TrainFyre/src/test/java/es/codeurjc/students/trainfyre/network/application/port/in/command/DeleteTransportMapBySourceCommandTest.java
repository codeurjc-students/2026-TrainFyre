package es.codeurjc.students.trainfyre.network.application.port.in.command;

import es.codeurjc.students.trainfyre.network.domain.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteTransportMapBySourceCommandTest {

    private static final DataSource SOURCE = new DataSource("renfe", "cercanias-madrid");

    @Test
    void shouldThrowNullPointerExceptionWhenSourceIsNull() {
        assertThatThrownBy(() -> new DeleteTransportMapBySourceCommand(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenSourceWhenItIsProvided() {
        DeleteTransportMapBySourceCommand command = new DeleteTransportMapBySourceCommand(SOURCE);

        assertThat(command.source()).isEqualTo(SOURCE);
    }
}
