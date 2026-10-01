package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.out.IncidencePort;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class IncidenceJPARepository implements IncidencePort {

    private final SpringDataIncidenceRepository repository;

    @Override
    public Void delete(UUID id) {
        repository.deleteById(id);
        return null;
    }

    @Override
    public void save(Incidence entity) {

    }

    @Override
    public Incidence findById(UUID id, Class<Incidence> incidenceClass) {
        return null;
    }

    @Override
    public PagedResponse<Incidence> findAll(Pageable pageable) {
        return null;
    }
}
