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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

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

    @Autowired
    private PlatformTransactionManager transactionManager;

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

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
            "unchanged user without subscriptions",
            "unchanged user with subscriptions",
            "added subscription",
            "removed subscription",
            "removed all subscriptions"
    })
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveShouldAdvanceUserVersionAfterCommit(String scenario) {
        var user = newTestUser();
        var first = subscription(1L);
        var second = subscription(2L);

        if (!scenario.equals("unchanged user without subscriptions")) {
            user.addAlertSubscription(first);
            user.addAlertSubscription(second);
        }

        try {
            newTransaction().executeWithoutResult(status ->
                    adapter.save(user)
            );

            var existingUser = loadUser(user.getId());
            var originalVersion = existingUser.getVersion();

            assertNotNull(originalVersion);

            switch (scenario) {
                case "unchanged user without subscriptions",
                     "unchanged user with subscriptions" -> {
                }
                case "added subscription" ->
                        existingUser.addAlertSubscription(subscription(3L));
                case "removed subscription" ->
                        existingUser.deleteAlertSubscription(first);
                case "removed all subscriptions" -> {
                    existingUser.deleteAlertSubscription(first);
                    existingUser.deleteAlertSubscription(second);
                }
                default -> throw new AssertionError(
                        "Unexpected scenario: " + scenario
                );
            }

            var expectedSubscriptions =
                    existingUser.getAlertSubscriptions();

            newTransaction().executeWithoutResult(status ->
                    adapter.save(existingUser)
            );

            var persistedUser = loadUser(user.getId());

            assertNotNull(persistedUser.getVersion());

            assertAll(
                    () -> assertTrue(
                            persistedUser.getVersion() > originalVersion,
                            "Saving an existing aggregate must advance its version"
                    ),
                    () -> assertEquals(
                            user.getUsername(),
                            persistedUser.getUsername()
                    ),
                    () -> assertEquals(
                            user.getPassword(),
                            persistedUser.getPassword()
                    ),
                    () -> assertEquals(
                            expectedSubscriptions,
                            persistedUser.getAlertSubscriptions()
                    )
            );
        } finally {
            deleteTestUser(user.getId());
        }
    }

    @ParameterizedTest(name = "[{index}] replaceWithEmptyList={0}")
    @ValueSource(booleans = {false, true})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveShouldDeleteReplacedSubscriptionsAndTheirCollectionRows(
            boolean replaceWithEmptyList
    ) {
        var user = newTestUser();
        user.addAlertSubscription(subscription(1L));
        user.addAlertSubscription(subscription(2L));

        try {
            newTransaction().executeWithoutResult(status ->
                    adapter.save(user)
            );

            List<UUID> originalSubscriptionIds =
                    newTransaction().execute(status ->
                            repository.findById(user.getId())
                                    .orElseThrow()
                                    .getAlertSubscriptions()
                                    .stream()
                                    .map(AlertSubscriptionEntity::getId)
                                    .toList()
                    );

            assertEquals(2, originalSubscriptionIds.size());

            var existingUser = loadUser(user.getId());

            for (var current : existingUser.getAlertSubscriptions()) {
                existingUser.deleteAlertSubscription(current);
            }

            if (!replaceWithEmptyList) {
                existingUser.addAlertSubscription(subscription(3L));
            }

            var expectedSubscriptions =
                    existingUser.getAlertSubscriptions();

            newTransaction().executeWithoutResult(status ->
                    adapter.save(existingUser)
            );

            var persistedUser = loadUser(user.getId());

            assertEquals(
                    expectedSubscriptions,
                    persistedUser.getAlertSubscriptions()
            );

            newTransaction().executeWithoutResult(status -> {
                for (UUID originalId : originalSubscriptionIds) {
                    assertSubscriptionRowsWereDeleted(originalId);
                }

                assertEquals(
                        replaceWithEmptyList ? 0L : 1L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscriptions
                                where user_id = ?1
                                """,
                                user.getId()
                        )
                );
            });
        } finally {
            deleteTestUser(user.getId());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveShouldRejectConcurrentSubscriptionReplacementAndPreserveWinner() {
        var user = newTestUser();
        var winningSubscription = subscription(1L);
        var rejectedSubscription = subscription(2L);

        try {
            newTransaction().executeWithoutResult(status ->
                    adapter.save(user)
            );

            var initialUser = loadUser(user.getId());
            var initialVersion = initialUser.getVersion();

            assertNotNull(initialVersion);

            assertThrows(
                    OptimisticLockingFailureException.class,
                    () -> newTransaction().executeWithoutResult(
                            firstTransaction -> {
                                var firstUser =
                                        repository.findById(user.getId())
                                                .orElseThrow()
                                                .toDomain();

                                assertEquals(
                                        initialVersion,
                                        firstUser.getVersion()
                                );

                                newTransaction().executeWithoutResult(
                                        secondTransaction -> {
                                            var secondUser =
                                                    repository.findById(
                                                                    user.getId()
                                                            )
                                                            .orElseThrow()
                                                            .toDomain();

                                            assertEquals(
                                                    firstUser.getVersion(),
                                                    secondUser.getVersion()
                                            );

                                            secondUser.addAlertSubscription(
                                                    winningSubscription
                                            );

                                            adapter.save(secondUser);
                                        }
                                );

                                firstUser.addAlertSubscription(
                                        rejectedSubscription
                                );

                                adapter.save(firstUser);
                            }
                    )
            );

            var persistedUser = loadUser(user.getId());

            assertNotNull(persistedUser.getVersion());

            assertAll(
                    () -> assertEquals(
                            List.of(winningSubscription),
                            persistedUser.getAlertSubscriptions()
                    ),
                    () -> assertEquals(
                            initialUser.getUsername(),
                            persistedUser.getUsername()
                    ),
                    () -> assertEquals(
                            initialUser.getPassword(),
                            persistedUser.getPassword()
                    ),
                    () -> assertTrue(
                            persistedUser.getVersion() > initialVersion
                    )
            );

            newTransaction().executeWithoutResult(status -> {
                assertEquals(
                        1L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscriptions
                                where user_id = ?1
                                """,
                                user.getId()
                        )
                );

                assertEquals(
                        0L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscription_lines l
                                join user_alert_subscriptions s
                                  on s.id = l.subscription_id
                                where s.user_id = ?1
                                  and l.line_id = 2
                                """,
                                user.getId()
                        ),
                        "The rejected subscription must not remain in the database"
                );
            });
        } finally {
            deleteTestUser(user.getId());
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
            "existing user without subscriptions",
            "existing user with ordered subscriptions",
            "missing user",
            "null id"
    })
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByIdShouldRespectItsContract(String scenario) {
        switch (scenario) {
            case "missing user" -> {
                assertThrows(
                        NoSuchElementException.class,
                        () -> adapter.findById(UUID.randomUUID(), User.class)
                );
                return;
            }
            case "null id" -> {
                assertThrows(
                        NullPointerException.class,
                        () -> adapter.findById(null, User.class)
                );
                return;
            }
            case "existing user without subscriptions",
                 "existing user with ordered subscriptions" -> {
            }
            default -> throw new AssertionError(
                    "Unexpected scenario: " + scenario
            );
        }

        var user = newTestUser();

        if (scenario.equals("existing user with ordered subscriptions")) {
            var start = ZonedDateTime.parse(
                    "2026-10-19T08:00:00+02:00[Europe/Madrid]"
            );

            user.addAlertSubscription(
                    new AlertSubscription(
                            Set.of(3L, 4L),
                            start.plusHours(3),
                            start.plusHours(5),
                            EnumSet.of(
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.FRIDAY
                            ),
                            start.plusMonths(2)
                    )
            );

            user.addAlertSubscription(
                    new AlertSubscription(
                            Set.of(1L, 2L),
                            start,
                            start.plusHours(2),
                            EnumSet.of(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.THURSDAY
                            ),
                            start.plusMonths(1)
                    )
            );
        }

        try {
            newTransaction().executeWithoutResult(status ->
                    adapter.save(user)
            );

            Long expectedVersion = newTransaction().execute(status ->
                    repository.findById(user.getId())
                            .orElseThrow()
                            .getVersion()
            );

            assertNotNull(expectedVersion);

            var foundUser = adapter.findById(user.getId(), User.class);

            assertNotNull(foundUser);

            assertAll(
                    () -> assertEquals(
                            user.getId(),
                            foundUser.getId()
                    ),
                    () -> assertEquals(
                            expectedVersion,
                            foundUser.getVersion()
                    ),
                    () -> assertEquals(
                            user.getUsername(),
                            foundUser.getUsername()
                    ),
                    () -> assertEquals(
                            user.getPassword(),
                            foundUser.getPassword()
                    ),
                    () -> assertEquals(
                            user.getAlertSubscriptions(),
                            foundUser.getAlertSubscriptions()
                    )
            );
        } finally {
            deleteTestUser(user.getId());
        }
    }

    private TransactionTemplate newTransaction() {
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );
        transaction.setIsolationLevel(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        return transaction;
    }

    private User loadUser(UUID id) {
        return newTransaction().execute(status ->
                repository.findById(id)
                        .orElseThrow(() -> new AssertionError(
                                "Expected the user to exist: " + id
                        ))
                        .toDomain()
        );
    }

    private User newTestUser() {
        return User.createUser(
                "test-" + UUID.randomUUID(),
                "encoded-password"
        );
    }

    private AlertSubscription subscription(long lineId) {
        var start = ZonedDateTime.parse(
                "2026-10-19T08:00:00+02:00[Europe/Madrid]"
        );

        return new AlertSubscription(
                Set.of(lineId),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY),
                start.plusMonths(1)
        );
    }

    private void deleteTestUser(UUID id) {
        newTransaction().executeWithoutResult(status ->
                repository.findById(id).ifPresent(repository::delete)
        );
    }

    private long countRows(String sql, UUID id) {
        return ((Number) entityManager.createNativeQuery(sql)
                .setParameter(1, id)
                .getSingleResult())
                .longValue();
    }

    private void assertSubscriptionRowsWereDeleted(UUID subscriptionId) {
        assertAll(
                () -> assertEquals(
                        0L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscriptions
                                where id = ?1
                                """,
                                subscriptionId
                        )
                ),
                () -> assertEquals(
                        0L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscription_lines
                                where subscription_id = ?1
                                """,
                                subscriptionId
                        )
                ),
                () -> assertEquals(
                        0L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscription_days
                                where subscription_id = ?1
                                """,
                                subscriptionId
                        )
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

    @ParameterizedTest(name = "[{index}] subscriptions={0}")
    @ValueSource(ints = {0, 1, 2})
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deleteShouldRemoveUserAndItsSubscriptions(
            int subscriptionCount
    ) {
        var user = newTestUser();
        var otherUser = newTestUser();

        for (int i = 1; i <= subscriptionCount; i++) {
            user.addAlertSubscription(subscription(i));
        }

        otherUser.addAlertSubscription(subscription(1L));
        otherUser.addAlertSubscription(subscription(2L));

        try {
            newTransaction().executeWithoutResult(status -> {
                adapter.save(user);
                adapter.save(otherUser);
            });

            List<UUID> subscriptionIds = newTransaction().execute(status ->
                    repository.findById(user.getId())
                            .orElseThrow()
                            .getAlertSubscriptions()
                            .stream()
                            .map(AlertSubscriptionEntity::getId)
                            .toList()
            );

            assertNotNull(subscriptionIds);
            assertEquals(subscriptionCount, subscriptionIds.size());

            var otherUserBeforeDelete = loadUser(otherUser.getId());

            assertNull(adapter.delete(user.getId()));

            newTransaction().executeWithoutResult(status -> {
                assertTrue(
                        repository.findById(user.getId()).isEmpty(),
                        "The deleted user must not remain in the database"
                );

                assertEquals(
                        0L,
                        countRows(
                                """
                                select count(*)
                                from user_alert_subscriptions
                                where user_id = ?1
                                """,
                                user.getId()
                        )
                );

                for (UUID subscriptionId : subscriptionIds) {
                    assertSubscriptionRowsWereDeleted(subscriptionId);
                }
            });

            var otherUserAfterDelete = loadUser(otherUser.getId());

            assertAll(
                    () -> assertEquals(
                            otherUserBeforeDelete.getId(),
                            otherUserAfterDelete.getId()
                    ),
                    () -> assertEquals(
                            otherUserBeforeDelete.getVersion(),
                            otherUserAfterDelete.getVersion()
                    ),
                    () -> assertEquals(
                            otherUserBeforeDelete.getUsername(),
                            otherUserAfterDelete.getUsername()
                    ),
                    () -> assertEquals(
                            otherUserBeforeDelete.getPassword(),
                            otherUserAfterDelete.getPassword()
                    ),
                    () -> assertEquals(
                            otherUserBeforeDelete.getAlertSubscriptions(),
                            otherUserAfterDelete.getAlertSubscriptions()
                    )
            );
        } finally {
            try {
                deleteTestUser(user.getId());
            } finally {
                deleteTestUser(otherUser.getId());
            }
        }
    }

}
