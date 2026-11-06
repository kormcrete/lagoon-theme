package io.github.kormcrete.flatlaf.lagoon;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * The status tones of {@link FlatLagoonModernLaf}, each a soft tint and the ink drawn on it: info (the accent),
 * success (teal), attention (amber), danger (red) and neutral (grey). Their colours are the UI defaults
 * {@code Lagoon.tone.<key>.background} and {@code .foreground}; their pills the style classes {@code pill-<key>}.
 */
public enum LagoonTone {
    INFO("info"),
    SUCCESS("success"),
    ATTENTION("attention"),
    DANGER("danger"),
    NEUTRAL("neutral");

    private final String key;

    LagoonTone(String key) {
        this.key = key;
    }

    /** The tone's name in UI default keys and style classes: {@code info}, {@code success}... */
    public String key() {
        return key;
    }

    /** The style class of a status pill in this tone, e.g. {@code pill-success}. */
    public String pillStyleClass() {
        return "pill-" + key;
    }

    /** The tint, {@code Lagoon.tone.<key>.background}; null outside the modern Lagoon. */
    public Color background() {
        return UIManager.getColor("Lagoon.tone." + key + ".background");
    }

    /** The ink on the tint, {@code Lagoon.tone.<key>.foreground}; null outside the modern Lagoon. */
    public Color foreground() {
        return UIManager.getColor("Lagoon.tone." + key + ".foreground");
    }
}
