package es.codeurjc.students.trainfyre.statistics.infrastructure.out;

import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.SpringDataIncidenceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SpringDataIncidenceRepositoryIntegrationTest {

    @Autowired
    private SpringDataIncidenceRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadIncidence() {
        Incidence incidence = anIncidence().build();

        Incidence saved = repository.save(incidence);
        entityManager.flush();

        UUID id = (UUID) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(saved);
        assertNotNull(id);

        entityManager.clear();

        Incidence reloaded = repository.findById(id).orElseThrow();

        assertEquals(id, entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(reloaded));
        assertEquals(incidence.getIncidenceDetails(),
                reloaded.getIncidenceDetails());
        assertTrue(repository.existsById(id));
    }
}