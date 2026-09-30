package es.codeurjc.students.trainfyre.common;

public record Pageable(int page, int size) {

    public Pageable {
        if(page < 0) throw new IllegalArgumentException("page should not be negative");
    }
}
