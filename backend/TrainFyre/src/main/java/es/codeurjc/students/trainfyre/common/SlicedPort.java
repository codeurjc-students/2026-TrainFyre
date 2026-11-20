package es.codeurjc.students.trainfyre.common;

/**
 * Output port that reads entities one slice at a time, without counting the total number of elements.
 * Prefer it to {@link PaginatedPort} when the caller only needs to know whether there is a next slice.
 *
 * @param <T> type of the entities in each slice
 */
public interface SlicedPort<T> {

    /**
     * Finds the slice at the page and size of the given {@link Pageable}. The first page is 0.
     * The result says whether another slice follows, but not how many elements exist in total.
     */
    SliceResponse<T> findSlice(Pageable pageable);
}
