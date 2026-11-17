package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record LineName(String shortName, String longName) {

    public LineName {
        String cleanShortName = stripOrNull(shortName);
        String cleanLongName = stripOrNull(longName);

        if (cleanShortName == null && cleanLongName == null) {
            throw new IllegalArgumentException("at least one of shortName and longName is required");
        }

        shortName = cleanShortName != null ? cleanShortName : cleanLongName;
        longName = cleanLongName != null ? cleanLongName : cleanShortName;
    }

    private static String stripOrNull(String raw) {
        return raw == null || raw.isBlank() ? null : raw.strip();
    }
}
