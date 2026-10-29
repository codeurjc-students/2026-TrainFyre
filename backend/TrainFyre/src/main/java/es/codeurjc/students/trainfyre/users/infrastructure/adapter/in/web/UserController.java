package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.users.application.port.in.CreateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.DeleteUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.GetUserByIdUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.UpdateUserUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.CreateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.command.DeleteUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;
import lombok.AllArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@PrimaryAdapter
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createUser(@RequestBody CreateUserCommand createUserCommand) {
        return createUserUseCase.execute(createUserCommand);
    }

    @GetMapping("/{id}")
    public UserDetails getUserById(@PathVariable("id") UUID id) {
        GetUserByIdQuery getUserByIdQuery = new GetUserByIdQuery(id);
        return getUserByIdUseCase.execute(getUserByIdQuery);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void updateUser(@RequestBody UpdateUserCommand updateUserCommand) {
        return updateUserUseCase.execute(updateUserCommand);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void deleteUser(@RequestBody DeleteUserCommand deleteUserCommand){
        return deleteUserUseCase.execute(deleteUserCommand);
    }
}
