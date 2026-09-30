package es.codeurjc.students.trainfyre.common;

import java.util.List;

public record PageRequest<T>(List<T> content, int page, int size, long totalElements) {
}
