package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SpringDataIncidenceRepository extends JpaRepository<IncidenceEntity, UUID> {

    @Query("""
        SELECT i
        FROM IncidenceEntity i
        ORDER BY i.id ASC
    """)
    Slice<IncidenceEntity> findAllSlice(Pageable pageable);
}