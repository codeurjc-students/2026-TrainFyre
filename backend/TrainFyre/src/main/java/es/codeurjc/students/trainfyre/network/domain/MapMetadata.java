package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.net.URI;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@ValueObject
public record MapMetadata(
        String name,
        ZoneId timezone,
        String country,
        URI website
) {

    private static final Set<String> ISO_ALPHA2_CODES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);

    public MapMetadata {
        Objects.requireNonNull(name, "name should not be null");
        Objects.requireNonNull(timezone, "timezone should not be null");

        if(name.isBlank()) throw new IllegalArgumentException("name should not bve blank");

        name = name.strip();

        if(country != null) {
            country = country.toUpperCase().trim();
        }

        if(country != null && country.isBlank()) throw new IllegalArgumentException("country cannot be blank");
        if(country != null && !ISO_ALPHA2_CODES.contains(country)){
            throw new IllegalArgumentException("country has to be an iso alpha 2 code");
        }


        if(website != null) {
            if(website.getScheme() == null) throw new IllegalArgumentException("if website is defined it should be a valid website");
            String protocol = website.getScheme();
            if(!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) throw new IllegalArgumentException("if website is defined it should be a valid website");
            if(website.getHost() == null) throw new IllegalArgumentException("if website is defined it should be a valid website");
        }

    }

}
