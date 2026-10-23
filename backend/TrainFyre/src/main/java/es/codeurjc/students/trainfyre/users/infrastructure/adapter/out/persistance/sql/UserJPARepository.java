package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@AllArgsConstructor
@SecondaryAdapter
@Repository
public class UserJPARepository implements UserPort {

    private final SpringDataUserRepository repository;

    @Override
    public Void delete(UUID id) {
        return null;
    }

    @Override
    public void save(User entity) {

    }

    @Override
    public User findById(UUID id, Class<User> userClass) {
        return null;
    }
}
