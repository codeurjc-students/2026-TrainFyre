package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Name of a line, with a short form (e.g. {@code "C1"}) and a long one (e.g. {@code "Cercanías Madrid C1"}).
 * Both are stripped, and a {@code null} or blank one counts as missing. At least one is required, and when
 * only one is given the other takes its value, so both are always present.
 */
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
