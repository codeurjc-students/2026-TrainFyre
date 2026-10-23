package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import es.codeurjc.students.trainfyre.users.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        UserJPARepository.class,
        UserJPARepositoryTest.DatabaseConfiguration.class
})
class UserJPARepositoryTest {

    @Autowired
    private UserJPARepository adapter;

    @Autowired
    private SpringDataUserRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveShouldPersistNewUserWithSubscription() {
        var start = ZonedDateTime.parse(
                "2026-10-19T08:00:00+02:00[Europe/Madrid]"
        );
        var subscription = new AlertSubscription(
                Set.of(1L, 2L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                start.plusMonths(1)
        );
        var user = User.createUser("pablo", "encoded-password");
        user.addAlertSubscription(subscription);

        adapter.save(user);

        entityManager.flush();
        entityManager.clear();

        var persistedUser = repository.findById(user.getId())
                .orElseThrow(() -> new AssertionError(
                        "Expected the user to be persisted"
                ))
                .toDomain();

        assertAll(
                () -> assertEquals(user.getId(), persistedUser.getId()),
                () -> assertEquals(
                        user.getUsername(),
                        persistedUser.getUsername()
                ),
                () -> assertEquals(
                        user.getPassword(),
                        persistedUser.getPassword()
                ),
                () -> assertEquals(
                        List.of(subscription),
                        persistedUser.getAlertSubscriptions()
                )
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DatabaseConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:18-alpine");
        }
    }

    @Test
    void saveShouldRejectNullUser() {
        assertThrows(
                NullPointerException.class,
                () -> adapter.save(null)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
            "unchanged user",
            "updated credentials",
            "added subscription",
            "removed subscription",
            "removed all subscriptions"
    })
    void saveShouldUpdateExistingUser(String scenario) {
        var start = ZonedDateTime.parse(
                "2026-10-19T08:00:00+02:00[Europe/Madrid]"
        );
        var firstSubscription = new AlertSubscription(
                Set.of(1L),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
        var secondSubscription = new AlertSubscription(
                Set.of(2L),
                start.plusHours(3),
                start.plusHours(5),
                EnumSet.of(DayOfWeek.WEDNESDAY),
                start.plusMonths(1)
        );
        var user = User.createUser("pablo", "encoded-password");
        user.addAlertSubscription(firstSubscription);
        user.addAlertSubscription(secondSubscription);

        adapter.save(user);

        entityManager.flush();
        entityManager.clear();

        var existingUser = repository.findById(user.getId())
                .orElseThrow(() -> new AssertionError(
                        "Expected the original user to be persisted"
                ))
                .toDomain();

        entityManager.clear();

        switch (scenario) {
            case "unchanged user" -> {
            }
            case "updated credentials" -> {
                existingUser.changeUsername("updated-username");
                existingUser.changePassword("updated-encoded-password");
            }
            case "added subscription" -> existingUser.addAlertSubscription(
                    new AlertSubscription(
                            Set.of(3L),
                            start.plusHours(6),
                            start.plusHours(8),
                            EnumSet.of(DayOfWeek.FRIDAY),
                            start.plusMonths(1)
                    )
            );
            case "removed subscription" ->
                    existingUser.deleteAlertSubscription(firstSubscription);
            case "removed all subscriptions" -> {
                existingUser.deleteAlertSubscription(firstSubscription);
                existingUser.deleteAlertSubscription(secondSubscription);
            }
            default -> throw new AssertionError(
                    "Unexpected scenario: " + scenario
            );
        }

        var expectedId = existingUser.getId();
        var expectedUsername = existingUser.getUsername();
        var expectedPassword = existingUser.getPassword();
        var expectedSubscriptions = existingUser.getAlertSubscriptions();

        adapter.save(existingUser);

        entityManager.flush();
        entityManager.clear();

        var persistedUser = repository.findById(expectedId)
                .orElseThrow(() -> new AssertionError(
                        "Expected the updated user to be persisted"
                ))
                .toDomain();

        assertAll(
                () -> assertEquals(expectedId, persistedUser.getId()),
                () -> assertEquals(
                        expectedUsername,
                        persistedUser.getUsername()
                ),
                () -> assertEquals(
                        expectedPassword,
                        persistedUser.getPassword()
                ),
                () -> assertEquals(
                        expectedSubscriptions,
                        persistedUser.getAlertSubscriptions()
                )
        );
    }


}
