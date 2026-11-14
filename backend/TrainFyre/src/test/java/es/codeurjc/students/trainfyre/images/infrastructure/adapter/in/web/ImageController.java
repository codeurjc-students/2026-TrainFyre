package es.codeurjc.students.trainfyre.images.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.images.application.port.DeleteUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.FindUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.UploadUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Delete;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.application.port.in.query.AvatarNotFoundException;
import es.codeurjc.students.trainfyre.images.application.port.in.query.Find;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@AllArgsConstructor
@PrimaryAdapter
@RestController
@RequestMapping("/api/v1/avatars")
public class ImageController {

    private final UploadUserAvatarUseCase uploadUserAvatarUseCase;
    private final FindUserAvatarUseCase findUserAvatarUseCase;
    private final DeleteUserAvatarUseCase deleteUserAvatarUseCase;

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            MultipartException.class
    })
    public ResponseEntity<Void> handleMissingFile() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(AvatarNotFoundException.class)
    public ResponseEntity<Void> handleAvatarNotFound() {
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void upload(@NonNull @PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser,@NonNull @RequestParam("file") MultipartFile file) {

        if(file.isEmpty()) {
            throw new IllegalArgumentException("image file cannot be empty");
        }

        try {

            InputStream inputStream = file.getInputStream();

            Image image = new Image(id, inputStream, file.getSize(), file.getContentType());

            Upload upload = new Upload(authenticatedUser, image);

            uploadUserAvatarUseCase.execute(upload);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void upload(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,@NonNull @RequestParam("file") MultipartFile file) {

        if(file.isEmpty()) {
            throw new IllegalArgumentException("image file cannot be empty");
        }

        try {

            InputStream inputStream = file.getInputStream();

            Image image = new Image(authenticatedUser.id(), inputStream, file.getSize(), file.getContentType());

            Upload upload = new Upload(authenticatedUser, image);

            uploadUserAvatarUseCase.execute(upload);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> find(@NonNull @PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return toResponse(findUserAvatarUseCase.execute(new Find(authenticatedUser, id)));

    }

    @GetMapping("/me")
    public ResponseEntity<InputStreamResource> find(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return toResponse(findUserAvatarUseCase.execute(new Find(authenticatedUser, authenticatedUser.id())));

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void delete(@NonNull @PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return deleteUserAvatarUseCase.execute(new Delete(authenticatedUser, id));

    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return deleteUserAvatarUseCase.execute(new Delete(authenticatedUser, authenticatedUser.id()));

    }

    /**
     * Devuelve los bytes de la imagen con su Content-Type y Content-Length.
     * InputStreamResource se cierra al terminar de escribir la respuesta.
     */
    private static ResponseEntity<InputStreamResource> toResponse(Image image) {

        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(mediaTypeOf(image.contentType()))
                .cacheControl(CacheControl.noCache().cachePrivate());

        // Con longitud 0 (desconocida) no se fija la cabecera para no truncar la respuesta.
        if (image.contentLength() > 0) {
            response.contentLength(image.contentLength());
        }

        return response.body(new InputStreamResource(image.content()));

    }

    /** Un Content-Type guardado que no sea válido no debe impedir servir la imagen. */
    private static MediaType mediaTypeOf(String contentType) {

        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

    }


}
