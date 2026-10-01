package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.out.persistance.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataIncidenceRepository extends JpaRepository<IncidenceEntity, UUID> {
}