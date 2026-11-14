package es.codeurjc.students.trainfyre.images.application.port;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import es.codeurjc.students.trainfyre.images.domain.Image;

public interface FindUserAvatarUseCase extends UseCase<Find, Image> {
}
