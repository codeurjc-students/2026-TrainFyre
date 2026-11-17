package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@ValueObject
public record HexColor(String value) {

    private static final String HASH_PREFIX = "#";
    private static final Pattern SIX_HEX_DIGITS = Pattern.compile("[A-F0-9]{6}");

    public HexColor {
        Objects.requireNonNull(value, "color should not be null");
        value = normalize(value);
        if (!SIX_HEX_DIGITS.matcher(value).matches()) {
            throw new IllegalArgumentException("not a six-digit hexadecimal color: " + value);
        }
    }

    private static String normalize(String raw) {
        String trimmed = raw.strip();
        if (trimmed.startsWith(HASH_PREFIX)) {
            trimmed = trimmed.substring(HASH_PREFIX.length());
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}
