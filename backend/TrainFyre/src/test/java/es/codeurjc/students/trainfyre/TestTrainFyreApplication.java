package es.codeurjc.students.trainfyre;

import org.springframework.boot.SpringApplication;

public class TestTrainFyreApplication {

    public static void main(String[] args) {
        SpringApplication.from(TrainFyreApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
