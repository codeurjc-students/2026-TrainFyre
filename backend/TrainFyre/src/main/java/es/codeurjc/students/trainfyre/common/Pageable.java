package es.codeurjc.students.trainfyre.common;

public record Pageable(int page, int size) {

    public Pageable {
        if(page < 0) throw new IllegalArgumentException("page should not be negative");
        if(size <= 0) throw new IllegalArgumentException("size should not be zero or less");
    }
}
