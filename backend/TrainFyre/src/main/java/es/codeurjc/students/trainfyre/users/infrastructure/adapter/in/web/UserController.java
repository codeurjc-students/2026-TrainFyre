package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.users.application.port.in.CreateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.DeleteUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.GetUserByIdUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.UpdateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;
import es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request.CreateUserRequest;
import es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@PrimaryAdapter
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@Valid @RequestBody CreateUserRequest request) {
        return createUserUseCase.execute(request.toCommand());
    }

    @GetMapping("/{id}")
    public UserDetails getUserById(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser requester) {
        GetUserByIdQuery getUserByIdQuery = new GetUserByIdQuery(requester, id);
        return getUserByIdUseCase.execute(getUserByIdQuery);
    }

    @GetMapping("/me")
    public UserDetails getUserById(@AuthenticationPrincipal AuthenticatedUser requester) {
        GetUserByIdQuery getUserByIdQuery = new GetUserByIdQuery(requester, requester.id());
        return getUserByIdUseCase.execute(getUserByIdQuery);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser requester
    ) {
        return updateUserUseCase.execute(request.toCommand(requester, id));
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser requester
    ) {
        return updateUserUseCase.execute(request.toCommand(requester, requester.id()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void deleteUser(@PathVariable("id") UUID id, @AuthenticationPrincipal AuthenticatedUser requester) {
        return deleteUserUseCase.execute(new DeleteUserCommand(requester, id));
    }
}
