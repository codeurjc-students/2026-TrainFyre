package es.codeurjc.students.trainfyre.common;

public interface EntityPort <T, K>{
    void save(T entity);
    T findById(K id, Class<T> tClass);
    Void delete(K id);
}
