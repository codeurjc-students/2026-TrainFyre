package es.codeurjc.students.trainfyre.statistics.infrastructure.out;

import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.IncidenceJPARepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
@Import(IncidenceJPARepository.class)
class IncidencePersistenceIntegrationTest {

    @Autowired
    private IncidenceJPARepository adapter;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadIncidence() {
        Incidence original = anIncidence().build();

        adapter.save(original);
        entityManager.flush();
        entityManager.clear();

        Incidence reloaded =
                adapter.findById(original.getId(), Incidence.class);

        assertEquals(original.getId(), reloaded.getId());
        assertEquals(original.getIncidenceDetails(),
                reloaded.getIncidenceDetails());
        assertEquals(
                original.getAffectedNetwork().lineIds(),
                reloaded.getAffectedNetwork().lineIds()
        );
    }
}
