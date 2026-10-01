package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;

import java.util.UUID;

@AllArgsConstructor
@SecondaryAdapter
public class IncidenceJPARepository implements IncidencePort {

    private final SpringDataIncidenceRepository repository;

    @Override
    public Void delete(UUID id) {
        repository.deleteById(id);
        return null;
    }

    @Override
    public void save(Incidence entity) {
        repository.save(entity);
    }

    @Override
    public Incidence findById(UUID id, Class<Incidence> incidenceClass) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public PagedResponse<Incidence> findAll(Pageable pageable) {
        return null;
    }
}
