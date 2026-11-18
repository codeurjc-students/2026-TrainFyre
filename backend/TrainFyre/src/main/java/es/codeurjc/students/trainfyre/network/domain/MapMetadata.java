package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.net.URI;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Descriptive, replaceable data of a {@link TransportMap}. The {@code country} is optional and, when present,
 * an ISO 3166-1 alpha-2 code normalized to uppercase. The {@code website} is optional and, when present,
 * an absolute http(s) URL.
 */
@ValueObject
public record MapMetadata(
        DataSource source,
        String name,
        ZoneId timezone,
        String country,
        URI website
) {

    private static final Set<String> ISO_ALPHA2_CODES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
    private static final Set<String> WEBSITE_SCHEMES = Set.of("http", "https");

    public MapMetadata {
        Objects.requireNonNull(source, "source should not be null");
        Objects.requireNonNull(name, "name should not be null");
        Objects.requireNonNull(timezone, "timezone should not be null");

        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name should not be empty or blank");
        }
        if (country != null) {
            country = normalizeCountry(country);
        }
        if (website != null) {
            requireHttpUrlWithHost(website);
        }
    }

    private static String normalizeCountry(String country) {
        String normalized = country.strip().toUpperCase(Locale.ROOT);
        if (!ISO_ALPHA2_CODES.contains(normalized)) {
            throw new IllegalArgumentException("country should be an ISO 3166-1 alpha-2 code");
        }
        return normalized;
    }

    private static void requireHttpUrlWithHost(URI website) {
        String scheme = website.getScheme();
        boolean isHttpUrl = scheme != null && WEBSITE_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT));
        if (!isHttpUrl || website.getHost() == null) {
            throw new IllegalArgumentException("website should be an http or https URL with a host");
        }
    }
}
