package es.codeurjc.students.trainfyre.statistics.infrastructure.out;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.IncidenceEntity;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.IncidenceJPARepository;
import es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql.SpringDataIncidenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static es.codeurjc.students.trainfyre.statistics.IncidenceTestBuilder.anIncidence;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        ArgumentCaptor<IncidenceEntity> captor =
                ArgumentCaptor.forClass(IncidenceEntity.class);
        verify(repository).save(captor.capture());

        IncidenceEntity saved = captor.getValue();
        assertEquals(incidence.getId(), saved.getId());
        assertEquals(incidence.getAffectedNetwork().mapId(), saved.getMapId());
        assertEquals(incidence.getAffectedNetwork().lineIds(), saved.getLineIds());
        assertEquals(incidence.getOccurrence().timestamp(), saved.getTimestamp());
        assertEquals(incidence.getOccurrence().duration(), saved.getDuration());
        assertEquals(incidence.getDescription().name(), saved.getName());
        assertEquals(incidence.getDescription().summary(), saved.getSummary());
        assertEquals(incidence.getClassification().severity(), saved.getSeverity());
        assertEquals(incidence.getClassification().cause(), saved.getCause());
    }


    @Test
    void shouldFindIncidenceById() {
        Incidence original = anIncidence().build();
        UUID id = original.getId();
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        when(repository.findById(id))
                .thenReturn(Optional.of(new IncidenceEntity(original)));

        Incidence result = adapter.findById(id, Incidence.class);

        assertNotSame(original, result);
        assertEquals(id, result.getId());
        assertEquals(original.getIncidenceDetails(), result.getIncidenceDetails());
        verify(repository).findById(id);
    }


    @Test
    void shouldFindIncidencesPaginated() {
        Incidence first = anIncidence().build();
        Incidence second = anIncidence().build();
        Pageable pageable = new Pageable(1, 2);
        PageRequest pageRequest = PageRequest.of(1, 2);
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        when(repository.findAll(pageRequest)).thenReturn(
                new PageImpl<>(
                        List.of(
                                new IncidenceEntity(first),
                                new IncidenceEntity(second)
                        ),
                        pageRequest,
                        5
                )
        );

        PagedResponse<Incidence> result = adapter.findAll(pageable);

        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5L, result.totalElements());
        assertEquals(2, result.content().size());

        assertEquals(first.getId(), result.content().get(0).getId());
        assertEquals(first.getIncidenceDetails(),
                result.content().get(0).getIncidenceDetails());
        assertEquals(second.getId(), result.content().get(1).getId());
        assertEquals(second.getIncidenceDetails(),
                result.content().get(1).getIncidenceDetails());

        verify(repository).findAll(pageRequest);
    }


    @Test
    void shouldFailWhenIncidenceDoesNotExist() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> adapter.findById(id, Incidence.class));

        verify(repository).findById(id);
    }

    @Test
    void shouldDescribeIncidenceNotFound() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);
        when(repository.findById(id)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> adapter.findById(id, Incidence.class)
        );

        assertEquals("Incidence not found: " + id, exception.getMessage());
        verify(repository).findById(id);
    }

    @Test
    void shouldRejectNullIdWhenDeleting() {
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.delete(null)
        );

        assertEquals("id should not be null", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullIncidenceWhenSaving() {
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.save(null)
        );

        assertEquals("incidence should not be null", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullIdWhenFindingById() {
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.findById(null, Incidence.class)
        );

        assertEquals("id should not be null", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullPageable() {
        IncidenceJPARepository adapter = new IncidenceJPARepository(repository);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> adapter.findAll(null)
        );

        assertEquals("pageable should not be null", exception.getMessage());
        verifyNoInteractions(repository);
    }


}