package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@SecondaryAdapter
@Repository
public class IncidenceJPARepository implements IncidencePort {

    private final SpringDataIncidenceRepository repository;
    private final IncidenceCountCache countCache;

    @Override
    public Void delete(UUID id) {
        Objects.requireNonNull(id, "id should not be null");
        repository.deleteById(id);
        countCache.invalidate();
        return null;
    }

    @Override
    public void save(Incidence incidence) {
        Objects.requireNonNull(incidence, "incidence should not be null");
        repository.save(new IncidenceEntity(incidence));
        countCache.invalidate();
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

        Slice<IncidenceEntity> result = repository.findAllSlice(
                PageRequest.of(pageable.page(), pageable.size())
        );

        List<Incidence> content = result.getContent()
                .stream()
                .map(IncidenceEntity::toDomain)
                .toList();

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                countCache.getTotalElements()
        );
    }

}
