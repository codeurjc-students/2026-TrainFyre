package es.codeurjc.students.trainfyre.common;

import java.util.List;
import java.util.Objects;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {

    public PagedResponse {
        Objects.requireNonNull(content, "content should not be null");
        if(page < 0) throw new IllegalArgumentException("page should not be negative");
        if(size < 0) throw new IllegalArgumentException("size should not be negative");
    }
}
