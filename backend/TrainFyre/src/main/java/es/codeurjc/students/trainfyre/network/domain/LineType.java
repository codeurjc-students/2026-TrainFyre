package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Transport mode of a line. Deliberately small: source-specific codes (e.g. GTFS route_type)
 * are mapped to these values by the import adapters, never by the domain.
 */
@ValueObject
public enum LineType {

    /** Tram, streetcar or light rail running on city streets or dedicated urban tracks. */
    TRAM,

    /** Underground or subway system inside a city. */
    METRO,

    /** Intercity, regional or commuter trains (e.g. Cercanías, Rodalies). */
    RAIL,

    /** Road buses, including coaches. */
    BUS,

    /** Boats and water transport. */
    FERRY,

    /** Any other mode: cable car, funicular, aerial lift, trolleybus, monorail, unknown. */
    OTHER
}
