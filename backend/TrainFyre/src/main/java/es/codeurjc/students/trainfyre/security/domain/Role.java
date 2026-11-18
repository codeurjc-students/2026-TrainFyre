package es.codeurjc.students.trainfyre.security.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Arrays;
import java.util.Optional;

@ValueObject
public enum Role {
    USER, ADMIN;

    public static Optional<Role> find(String name) {
        return Arrays.stream(values())
                .filter(role -> role.name().equals(name))
                .findFirst();
    }
}
