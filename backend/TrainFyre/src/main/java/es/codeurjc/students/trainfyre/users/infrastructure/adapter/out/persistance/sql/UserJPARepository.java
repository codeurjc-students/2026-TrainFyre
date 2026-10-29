package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@SecondaryAdapter
@Repository
public class UserJPARepository implements UserPort {

    private final SpringDataUserRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Void delete(UUID id) {
        return null;
    }

    @Override
    @Transactional
    public void save(User user) {
        Objects.requireNonNull(user, "User should not be null");

        boolean existingUser = user.getVersion() != null;

        UserEntity managedEntity =
                repository.save(new UserEntity(user));

        if (existingUser) {
            entityManager.lock(
                    managedEntity,
                    LockModeType.OPTIMISTIC_FORCE_INCREMENT
            );
        }
    }


    @Override
    public User findById(UUID id, Class<User> userClass) {
        return null;
    }
}
