package es.codeurjc.students.trainfyre.users.infrastructure.adapter.out.persistance.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
}
