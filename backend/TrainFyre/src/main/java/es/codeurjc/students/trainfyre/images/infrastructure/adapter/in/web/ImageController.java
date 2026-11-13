package es.codeurjc.students.trainfyre.images.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.images.application.port.UploadUserAvatarUseCase;
import es.codeurjc.students.trainfyre.images.application.port.in.command.Upload;
import es.codeurjc.students.trainfyre.images.domain.Image;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/images")
public class ImageController {

    private final UploadUserAvatarUseCase uploadUserAvatarUseCase;

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            MultipartException.class
    })
    public ResponseEntity<Void> handleMissingFile() {
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}/avatar")
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

    @PutMapping("/me/avatar")
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


}
