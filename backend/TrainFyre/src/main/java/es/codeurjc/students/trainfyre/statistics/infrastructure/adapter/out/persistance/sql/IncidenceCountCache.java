package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncidenceCountCache {

    private final SpringDataIncidenceRepository repository;

    @Cacheable(
            cacheNames = "incidence-count",
            sync = true
    )
    public long getTotalElements() {
        return repository.count();
    }

    @CacheEvict(
            cacheNames = "incidence-count",
            allEntries = true
    )
    public void invalidate() {
    }
}
