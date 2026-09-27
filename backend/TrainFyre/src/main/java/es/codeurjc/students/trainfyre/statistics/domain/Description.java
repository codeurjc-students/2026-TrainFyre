package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Description (String name, String summary){


    public Description {
        if(name == null) throw new IllegalArgumentException("name should not be null");
        if(summary == null) throw new IllegalArgumentException("summary should not be null");
    }

}
