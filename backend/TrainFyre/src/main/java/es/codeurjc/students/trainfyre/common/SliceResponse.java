package es.codeurjc.students.trainfyre.common;

import java.util.List;
import java.util.Objects;

public record SliceResponse<T>(List<T> content, int page, boolean hasNext) {


    public SliceResponse {

        Objects.requireNonNull(content, "content should not be null");
        if (page < 0) throw new IllegalArgumentException("page should not be negative");
        if (content.isEmpty() && hasNext) throw new IllegalArgumentException("cannot have next when content is empty");

        content = List.copyOf(content);

    }

    public List<T> content() {
        return List.copyOf(content);
    }
}
