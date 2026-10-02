package es.codeurjc.students.trainfyre.statistics.load;

import es.codeurjc.students.trainfyre.TrainFyreApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mariadb.MariaDBContainer;

public class TestMariaDbLoadApplication {

    public static void main(String[] args) {
        SpringApplication.from(TrainFyreApplication::main)
                .with(Containers.class)
                .run(
                        "--server.port=8080",
                        "--spring.jpa.hibernate.ddl-auto=create-drop"
                );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Containers {

        @Bean
        @ServiceConnection
        MariaDBContainer mariaDb() {
            return new MariaDBContainer("mariadb:11.4");
        }
    }
}
