package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.UploadUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UploadUserAvatarService implements UploadUserAvatarUseCase {

    private final ImagePort imagePort;

    @CommandHandler
    @Override
    public Void execute(Upload input) {


        return null;
    }
}
