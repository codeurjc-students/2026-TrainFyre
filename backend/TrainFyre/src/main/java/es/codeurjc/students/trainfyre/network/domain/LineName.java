package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record LineName(String shortName, String longName) {

    public LineName {

        if((shortName == null || shortName.trim() == "") && (longName == null || longName.trim() == "")) throw new IllegalArgumentException("both arguments cannot be null, at least one");

        if((shortName != null && shortName.trim() != "") && (longName == null || longName.trim() == "")) {
            shortName = shortName.strip();
            longName = shortName.strip();
        } else if ((shortName == null || shortName.trim() == "") && (longName != null && longName.trim() != "")) {
            shortName = longName.strip();
            longName = longName.strip();
        }
        else {
            shortName = shortName.strip();
            longName = longName.strip();
        }


    }
}
