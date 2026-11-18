package es.codeurjc.students.trainfyre.network.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

/**
 * Colors of a line: {@code color} is the background and {@code textColor} the text drawn over it.
 */
@ValueObject
public record LineColors(HexColor color, HexColor textColor) {

    private static final int DEGREES = 360;
    private static final int HUE_STEP = 137;
    private static final double SATURATION = 0.45;
    private static final double BRIGHTNESS = 0.95;
    private static final int MAX_CHANNEL = 255;
    private static final int SEXTANTS = 6;
    private static final double SEXTANT_DEGREES = DEGREES / (double) SEXTANTS;
    private static final int RED_OFFSET = 5;
    private static final int GREEN_OFFSET = 3;
    private static final int BLUE_OFFSET = 1;
    private static final HexColor DEFAULT_TEXT_COLOR = new HexColor("000000");

    public LineColors {
        Objects.requireNonNull(color, "color should not be null");
        Objects.requireNonNull(textColor, "textColor should not be null");
    }

    /**
     * Deterministic default colors for a line: a pastel background whose hue is derived from the external id
     * (consecutive ids get well separated hues) and black text.
     *
     * @throws NullPointerException if {@code externalId} is {@code null}
     */
    public static LineColors defaultFor(String externalId) {
        Objects.requireNonNull(externalId, "external id should not be null");

        int hue = Math.floorMod(externalId.hashCode(), DEGREES) * HUE_STEP % DEGREES;
        return new LineColors(backgroundWithHue(hue), DEFAULT_TEXT_COLOR);
    }

    private static HexColor backgroundWithHue(int hue) {
        return new HexColor("%02X%02X%02X".formatted(
                channel(hue, RED_OFFSET), channel(hue, GREEN_OFFSET), channel(hue, BLUE_OFFSET)));
    }

    /** HSB to RGB conversion for one channel, with the fixed saturation and brightness. */
    private static int channel(int hue, int offset) {
        double k = (offset + hue / SEXTANT_DEGREES) % SEXTANTS;
        double factor = Math.max(0, Math.min(Math.min(k, 4 - k), 1));
        double value = BRIGHTNESS * (1 - SATURATION * factor);
        return (int) Math.round(value * MAX_CHANNEL);
    }
}
