package es.codeurjc.students.trainfyre.images.application.service;

import es.codeurjc.students.trainfyre.images.application.port.FindUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import es.codeurjc.students.trainfyre.images.application.port.out.ImagePort;
import es.codeurjc.students.trainfyre.images.domain.Image;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindUserAvatarService implements FindUserAvatarUseCase {


    private final ImagePort imagePort;

    @CommandHandler
    @Override
    public Image execute(Find input) {

        input.assertCanFindAvatar();

        return imagePort.find(input.uuid()).orElseThrow();

    }
}
