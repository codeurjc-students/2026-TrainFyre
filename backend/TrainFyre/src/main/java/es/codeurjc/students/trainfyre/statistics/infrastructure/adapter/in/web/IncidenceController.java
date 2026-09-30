package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UUID> createIncidence(@RequestBody CreateIncidenceCommand createIncidenceCommand) {
        UUID id =  createIncidenceUseCase.execute(createIncidenceCommand);
        return ResponseEntity.status(201).body(id);
    }


}
