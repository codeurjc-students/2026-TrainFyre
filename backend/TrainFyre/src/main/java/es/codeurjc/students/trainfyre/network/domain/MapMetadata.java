package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.net.URI;
import java.time.ZoneId;

@ValueObject
public record MapMetadata(
        String name,
        ZoneId timezone,
        String country,
        URI website
) { }
