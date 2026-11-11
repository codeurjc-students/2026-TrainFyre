package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.DeleteUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteUserAvatarService implements DeleteUserAvatarUseCase {

    private final ImagePort imagePort;

    @CommandHandler
    @Override
    public Void execute(Delete input) {
        return null;
    }
}
