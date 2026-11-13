package es.codeurjc.students.trainfyre.images.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.images.application.port.UploadUserAvatarUseCase;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@AllArgsConstructor
@PrimaryAdapter
@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private final UploadUserAvatarUseCase uploadUserAvatarUseCase;


    @PutMapping("/{id}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void upload(@NonNull @PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser,@NonNull @RequestParam("file") MultipartFile file) {
        return null;
    }

}
