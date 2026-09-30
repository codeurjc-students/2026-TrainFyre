package es.codeurjc.students.trainfyre.common;

public interface PaginatedPort <T> {
    PageRequest<T> findAll(Pageable pageable);
}
