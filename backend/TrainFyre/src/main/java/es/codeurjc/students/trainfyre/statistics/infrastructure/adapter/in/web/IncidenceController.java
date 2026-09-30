package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.CreateIncidenceUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.GetAllIncidencesPaginatedUseCase;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PrimaryAdapter
@RestController
@RequestMapping("/incidence")
public class IncidenceController {

    private final CreateIncidenceUseCase createIncidenceUseCase;
    private final GetAllIncidencesPaginatedUseCase getAllIncidencesPaginatedUseCase;

    public IncidenceController(CreateIncidenceUseCase createIncidenceUseCase, GetAllIncidencesPaginatedUseCase getAllIncidencesPaginatedUseCase) {
        this.createIncidenceUseCase = createIncidenceUseCase;
        this.getAllIncidencesPaginatedUseCase = getAllIncidencesPaginatedUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createIncidence(@RequestBody CreateIncidenceCommand createIncidenceCommand) {
        return createIncidenceUseCase.execute(createIncidenceCommand);
    }

    @GetMapping
    public PagedResponse<Incidence> getAllIncidencesPaginated(@RequestParam int page, @RequestParam int size){
        Pageable pageable = new Pageable(page, size);
        GetAllIncidencesPaginatedQuery getAllIncidencesPaginatedQuery = new GetAllIncidencesPaginatedQuery(pageable);
        return getAllIncidencesPaginatedUseCase.execute(getAllIncidencesPaginatedQuery);
    }


}
