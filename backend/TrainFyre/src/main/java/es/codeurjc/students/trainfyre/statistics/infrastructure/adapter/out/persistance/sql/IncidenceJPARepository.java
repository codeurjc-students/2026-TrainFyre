package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@SecondaryAdapter
public class IncidenceJPARepository implements IncidencePort {

    private final SpringDataIncidenceRepository repository;

    @Override
    public Void delete(UUID id) {
        Objects.requireNonNull(id, "id should not be null");
        repository.deleteById(id);
        return null;
    }

    @Override
    public void save(Incidence incidence) {
        Objects.requireNonNull(incidence, "incidence should not be null");
        repository.save(new IncidenceEntity(incidence));
    }

    @Override
    public Incidence findById(UUID id, Class<Incidence> incidenceClass) {
        Objects.requireNonNull(id, "id should not be null");

        return repository.findById(id)
                .map(IncidenceEntity::toDomain)
                .orElseThrow(() ->
                        new NoSuchElementException("Incidence not found: " + id));
    }

    @Override
    public PagedResponse<Incidence> findAll(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable should not be null");

        Page<Incidence> result = repository
                .findAll(PageRequest.of(pageable.page(), pageable.size()))
                .map(IncidenceEntity::toDomain);

        return new PagedResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        );
    }

}
