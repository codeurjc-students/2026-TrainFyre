package es.codeurjc.students.trainfyre.common;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {

    public PagedResponse {
        if(page < 0) throw new IllegalArgumentException("page should not be negative");
    }
}
