package es.codeurjc.students.trainfyre.common;

public interface PaginatedPort <T> {
    PagedResponse<T> findAll(Pageable pageable);
}
