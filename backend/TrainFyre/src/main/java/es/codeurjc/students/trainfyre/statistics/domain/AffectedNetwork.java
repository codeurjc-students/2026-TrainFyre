package es.codeurjc.students.trainfyre.statistics.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record AffectedNetwork(Long mapId, List<Long> lineIds) {

    public AffectedNetwork{
        if(mapId == null) throw  new IllegalArgumentException("map id should not be null");
        if(lineIds == null) throw new IllegalArgumentException("lineIds should not be null");
        if(lineIds.isEmpty()) throw new IllegalArgumentException("lineIds should not be an empty list");
    }
}
