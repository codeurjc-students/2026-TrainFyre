package es.codeurjc.students.trainfyre;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ArchitectureTest {

    @Test
    void verify_architecture_and_generate_documentation() {
        
        var modules = ApplicationModules
                .of(TrainFyreApplication.class)
                .verify();

        var diagramOptions = Documenter.DiagramOptions.defaults();
        var canvasOptions = Documenter.CanvasOptions.defaults().revealInternals();

        new Documenter(modules)
                .writeModulesAsPlantUml(diagramOptions)
                .writeIndividualModulesAsPlantUml(diagramOptions)
                .writeModuleCanvases(canvasOptions)
                .writeAggregatingDocument(diagramOptions, canvasOptions);
    }
}
