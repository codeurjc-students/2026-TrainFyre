package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataIncidenceRepository extends JpaRepository<Incidence, UUID> {
}