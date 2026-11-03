package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.users.application.port.in.LoginUseCase;
import es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request.LoginRequest;
import es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PrimaryAdapter
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(loginUseCase.execute(request.toCommand()));
    }
}
