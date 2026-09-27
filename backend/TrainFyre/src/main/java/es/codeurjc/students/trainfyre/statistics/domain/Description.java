package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Description (String name, String summary){}
