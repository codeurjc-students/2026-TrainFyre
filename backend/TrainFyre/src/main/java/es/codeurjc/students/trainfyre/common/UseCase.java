package es.codeurjc.students.trainfyre.common;

public interface UseCase <I,O> {

    O execute(I input);
}
