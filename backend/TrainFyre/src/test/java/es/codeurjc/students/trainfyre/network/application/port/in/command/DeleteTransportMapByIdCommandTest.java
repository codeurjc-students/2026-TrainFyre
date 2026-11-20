package es.codeurjc.students.trainfyre.network.application.port.in.command;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteTransportMapByIdCommandTest {

    private static final UUID ID = UUID.fromString("b3f1c2d4-5e6a-4b7c-8d9e-0a1b2c3d4e5f");

    @Test
    void shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new DeleteTransportMapByIdCommand(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExposeTheGivenIdWhenItIsProvided() {
        DeleteTransportMapByIdCommand command = new DeleteTransportMapByIdCommand(ID);

        assertThat(command.id()).isEqualTo(ID);
    }
}
