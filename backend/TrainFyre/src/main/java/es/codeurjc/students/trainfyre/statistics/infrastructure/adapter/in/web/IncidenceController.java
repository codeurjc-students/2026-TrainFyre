package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PrimaryAdapter
@RestController
@RequestMapping("/incidence")
public class IncidenceController {

    private final CreateIncidenceUseCase createIncidenceUseCase;

    public IncidenceController(CreateIncidenceUseCase createIncidenceUseCase) {
        this.createIncidenceUseCase = createIncidenceUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createIncidence(@RequestBody CreateIncidenceCommand createIncidenceCommand) {
        return createIncidenceUseCase.execute(createIncidenceCommand);
    }


}
