package es.codeurjc.students.trainfyre.statistics.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.common.Pageable;
import es.codeurjc.students.trainfyre.common.PagedResponse;
import es.codeurjc.students.trainfyre.statistics.application.port.in.*;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.CreateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.DeleteIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.command.UpdateIncidenceCommand;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetAllIncidencesPaginatedQuery;
import es.codeurjc.students.trainfyre.statistics.application.port.in.query.GetIncidenceByIdQuery;
import es.codeurjc.students.trainfyre.statistics.application.service.UpdateIncidenceService;
import es.codeurjc.students.trainfyre.statistics.domain.Incidence;
import es.codeurjc.students.trainfyre.statistics.domain.IncidenceDetails;
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
    private final GetIncidenceByIdUseCase getIncidenceByIdUseCase;
    private final UpdateIncidenceUseCase updateIncidenceUseCase;
    private final DeleteIncidenceUseCase deleteIncidenceUseCase;

    public IncidenceController(CreateIncidenceUseCase createIncidenceUseCase, GetAllIncidencesPaginatedUseCase getAllIncidencesPaginatedUseCase, GetIncidenceByIdUseCase getIncidenceByIdUseCase, UpdateIncidenceUseCase updateIncidenceUseCase, DeleteIncidenceUseCase deleteIncidenceUseCase) {
        this.createIncidenceUseCase = createIncidenceUseCase;
        this.getAllIncidencesPaginatedUseCase = getAllIncidencesPaginatedUseCase;
        this.getIncidenceByIdUseCase = getIncidenceByIdUseCase;
        this.updateIncidenceUseCase = updateIncidenceUseCase;
        this.deleteIncidenceUseCase = deleteIncidenceUseCase;
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

    @GetMapping("/{id}")
    public IncidenceDetails getIncidenceById(@PathVariable("id") UUID id){
        GetIncidenceByIdQuery getIncidenceByIdQuery = new GetIncidenceByIdQuery(id);
        return getIncidenceByIdUseCase.execute(getIncidenceByIdQuery);
    }

   @PutMapping
   @ResponseStatus(HttpStatus.NO_CONTENT)
    public Void updateIncidence(@RequestBody UpdateIncidenceCommand updateIncidenceCommand){
        return updateIncidenceUseCase.execute(updateIncidenceCommand);
   }

   @DeleteMapping
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public Void deleteIncidence(@RequestBody DeleteIncidenceCommand deleteIncidenceCommand){
        return deleteIncidenceUseCase.execute(deleteIncidenceCommand);
   }
}
