package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.util.ColorFunctions;

import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;

/**
 * The shade of scroll bars and tabs, chosen by {@code Lagoon.shade}: {@code gloss} (the default), {@code soft}
 * ({@link FlatLagoonSoftLaf}) or {@code flat}. Every shade is worked out from the colour the part would otherwise
 * be filled with, so hover and pressed states keep their shading. Plain gradients: no images, nothing cached.
 */
final class LagoonShade {

    static final String GLOSS = "gloss";
    static final String SOFT = "soft";
    static final String FLAT = "flat";

    private LagoonShade() {
    }

    /** The shade in {@code Lagoon.shade}; flat when the key is missing or unknown. */
    static String current() {
        String shade = UIManager.getString("Lagoon.shade");
        return GLOSS.equals(shade) || SOFT.equals(shade) ? shade : FLAT;
    }

    /**
     * A thumb's fill across its breadth (left to right on a vertical bar, top to bottom on a horizontal one).
     * Gloss: a tube, light on the leading side, darkest along the middle, glowing again towards the far side;
     * soft: light to dark; flat: {@code null}, the colour as it is.
     */
    static Paint thumb(String shade, Color base, Rectangle r, boolean vertical) {
        if (GLOSS.equals(shade)) {
            Color glow = lighten(ColorFunctions.saturate(ColorFunctions.spin(base, -12), .25f), 2);
            return across(r, vertical, new float[]{0f, .12f, .5f, .82f, 1f},
                    lighten(base, 4), lighten(base, 8), darken(base, 13), glow, darken(base, 1));
        }
        if (SOFT.equals(shade)) {
            return across(r, vertical, new float[]{0f, .45f, 1f}, lighten(base, 7), base, darken(base, 7));
        }
        return null;
    }

    /**
     * A track's groove: a shadow on the side facing the content ({@code fromFar}: the right of a vertical bar laid
     * out right to left) fading into the track by 60% of its breadth. {@code null} when flat.
     */
    static Paint groove(String shade, Color track, Rectangle r, boolean vertical, boolean fromFar) {
        if (FLAT.equals(shade)) {
            return null;
        }
        Color[] colors = {darken(track, 7), lighten(track, 1), lighten(track, 2)};
        if (fromFar) {
            colors = new Color[]{colors[2], colors[1], colors[0]};
            return across(r, vertical, new float[]{0f, .4f, 1f}, colors);
        }
        return across(r, vertical, new float[]{0f, .6f, 1f}, colors);
    }

    /**
     * An unselected tab's fill from its outer edge to the content. Gloss: a pillow, light under the outer edge,
     * darker past the middle, a little lighter again at the content; soft and flat: {@code light} to
     * {@code bottom}, Lagoon's plain shade.
     */
    static Paint tab(String shade, Color light, Color bottom, Rectangle r, int placement) {
        Color[] colors;
        float[] stops;
        if (GLOSS.equals(shade)) {
            stops = new float[]{0f, .12f, .55f, 1f};
            colors = new Color[]{lighten(light, 3), lighten(light, 1), darken(bottom, 2), lighten(bottom, 5)};
        } else {
            stops = new float[]{0f, 1f};
            colors = new Color[]{light, bottom};
        }
        float x0 = r.x, y0 = r.y, x1 = r.x, y1 = r.y;
        switch (placement) {
            case SwingConstants.BOTTOM: y0 = r.y + r.height; break;
            case SwingConstants.LEFT:   x1 = r.x + r.width; break;
            case SwingConstants.RIGHT:  x0 = r.x + r.width; break;
            default:                    y1 = r.y + r.height; break;
        }
        if (x0 == x1 && y0 == y1) {
            return colors[colors.length - 1];
        }
        return new LinearGradientPaint(x0, y0, x1, y1, stops, colors);
    }

    private static Paint across(Rectangle r, boolean vertical, float[] stops, Color... colors) {
        int breadth = vertical ? r.width : r.height;
        if (breadth < 2) {
            return colors[colors.length / 2];
        }
        return vertical
                ? new LinearGradientPaint(r.x, 0, r.x + breadth, 0, stops, colors)
                : new LinearGradientPaint(0, r.y, 0, r.y + breadth, stops, colors);
    }

    private static Color lighten(Color c, float percent) {
        return ColorFunctions.lighten(c, percent / 100f);
    }

    private static Color darken(Color c, float percent) {
        return ColorFunctions.darken(c, percent / 100f);
    }
}
