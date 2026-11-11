package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//class DeleteUserAvatarServiceTest {
//
//    private final ImagePort imagePort = mock(ImagePort.class);
//
//    private final DeleteUserAvatarService service = new DeleteUserAvatarService(imagePort);
//
//    @Test
//    void shouldDeleteImageWhenUserIsOwner() {
//        // Given
//        UUID imageUuid = UUID.randomUUID();
//        Delete command = new Delete(imageUuid);
//
//        // When
//        service.execute(command);
//
//        // Then
//        verify(imagePort).delete(imageUuid);
//    }
//
//    @Test
//    void shouldDeleteImageWhenAdminUser() {
//        // Given
//        UUID imageUuid = UUID.randomUUID();
//        Delete command = new Delete(imageUuid);
//
//        // When
//        service.execute(command);
//
//        // Then
//        verify(imagePort).delete(imageUuid);
//    }
//
//    @Test
//    void shouldPropagateExceptionWhenDeletingFails() {
//        // Given
//        UUID imageUuid = UUID.randomUUID();
//        Delete command = new Delete(imageUuid);
//
//        RuntimeException persistenceFailure =
//                new RuntimeException("Could not delete image");
//
//        doThrow(persistenceFailure)
//                .when(imagePort)
//                .delete(imageUuid);
//
//        // When
//        RuntimeException thrown = assertThrows(
//                RuntimeException.class,
//                () -> service.execute(command)
//        );
//
//        // Then
//        assertSame(persistenceFailure, thrown);
//    }
//}
