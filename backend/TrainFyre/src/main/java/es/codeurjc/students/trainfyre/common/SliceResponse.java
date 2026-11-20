package es.codeurjc.students.trainfyre.common;

import java.util.List;

public record SliceResponse<T>(List<T> content, int page, boolean hasNext) { }
