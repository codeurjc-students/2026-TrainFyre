package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public record LineColors(HexColor color, HexColor textColor) {

    public LineColors {
        Objects.requireNonNull(color, "color should not be null");
        Objects.requireNonNull(textColor, "textColor should not be null");
    }

    public static LineColors defaultFor(String externalId) {

        int hue = Math.floorMod(externalId.hashCode(), 360) * 137 % 360;
        int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 0.45f, 0.95f) & 0xFFFFFF;
        HexColor background = new HexColor("%06X".formatted(rgb));
        return new LineColors(background, new HexColor("000000"));

    }

}
