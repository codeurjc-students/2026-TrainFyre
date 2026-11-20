package es.codeurjc.students.trainfyre.network.application.service;

import es.codeurjc.students.trainfyre.network.application.port.in.DeleteTransportMapByIdUseCase;
import es.codeurjc.students.trainfyre.network.application.port.in.command.DeleteTransportMapByIdCommand;
import es.codeurjc.students.trainfyre.network.application.port.out.TransportMapPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTransportMapByIdServiceTest {

    private static final UUID MAP_ID = UUID.fromString("b3f1c2d4-5e6a-4b7c-8d9e-0a1b2c3d4e5f");

    @Mock
    private TransportMapPort transportMapPort;
    private DeleteTransportMapByIdUseCase deleteTransportMapByIdUseCase;

    @BeforeEach
    void setUp() {
        deleteTransportMapByIdUseCase = new DeleteTransportMapByIdService(transportMapPort);
    }

    @Test
    void shouldDeleteTheMapByIdWhenCommandIsValid() {
        Void result = deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(MAP_ID));

        assertThat(result).isNull();
        verify(transportMapPort).delete(MAP_ID);
    }

    @Test
    void shouldPropagateExceptionWhenDeletingFails() {
        RuntimeException persistenceFailure = new RuntimeException("Could not delete transport map");
        when(transportMapPort.delete(MAP_ID)).thenThrow(persistenceFailure);

        assertThatThrownBy(() -> deleteTransportMapByIdUseCase.execute(new DeleteTransportMapByIdCommand(MAP_ID)))
                .isSameAs(persistenceFailure);
    }
}
