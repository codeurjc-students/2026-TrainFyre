package es.codeurjc.students.trainfyre.statistics.infrastructure.out;

import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.IncidenceJPARepository;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.SpringDataIncidenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IncidenceJPARepositoryTest {

    @Mock
    private SpringDataIncidenceRepository repository;

    @Test
    void shouldDeleteIncidenceById() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        adapter.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void shouldSaveIncidence() {
        Incidence incidence = anIncidence().build();
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        adapter.save(incidence);

        verify(repository).save(incidence);
    }

}