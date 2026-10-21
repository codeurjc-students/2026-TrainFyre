package es.codeurjc.students.trainfyre.users.application.port.out;

import es.codeurjc.students.trainfyre.common.EntityPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.util.UUID;

@SecondaryPort
public interface UserPort extends EntityPort<User, UUID> {
}
