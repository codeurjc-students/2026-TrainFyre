package es.codeurjc.students.trainfyre.common;

public interface SlicedPort <T> {

    SliceResponse<T> findSlice(Pageable pageable);

}
