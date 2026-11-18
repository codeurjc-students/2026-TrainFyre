package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum Cause {
    WEATHER, MAINTENANCE, STRIKE, TECHNICAL_PROBLEM, MEDICAL_EMERGENCY, POLICE_ACTION, HOLIDAY, OTHER
}
