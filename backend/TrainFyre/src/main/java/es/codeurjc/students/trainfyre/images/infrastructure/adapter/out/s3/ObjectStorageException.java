package es.codeurjc.students.trainfyre.images.infrastructure.adapter.out.s3;

public class ObjectStorageException extends RuntimeException {

    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
