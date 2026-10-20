package es.codeurjc.students.trainfyre.users.domain;

import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.Objects;
import java.util.UUID;

@AggregateRoot
public class User {

    private final UUID id;
    private String username;
    private String password;


    private User(UUID id, String username, String password){

        this.id = id;
        this.username = username;
        this.password = password;

    }

    public static User createUser(String username, String password){

        Objects.requireNonNull(username, "Username should not be null");
        Objects.requireNonNull(password, "Password should not be null");

        UUID id = UUID.randomUUID();

        return new User(id, username, password);

    }

}
