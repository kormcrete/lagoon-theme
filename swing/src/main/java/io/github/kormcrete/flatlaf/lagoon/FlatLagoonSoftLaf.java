package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;

/**
 * Lagoon with the quieter Soft shade: scroll thumbs shaded plainly from light to dark across their breadth,
 * with smaller round ends, and tabs keeping a plain shade towards the content. Everything else is
 * {@link FlatLagoonLaf}'s; the differences live in {@code FlatLagoonSoftLaf.properties}.
 *
 * <p>Install with {@link #setup()}, or {@code FlatLaf.setup(new FlatLagoonSoftLaf())}.
 */
public class FlatLagoonSoftLaf extends FlatLagoonLaf {

    public static final String NAME = "Lagoon Soft";

    /**
     * Installs the theme, as {@link FlatLaf#setup(javax.swing.LookAndFeel)} does.
     *
     * @return whether the look-and-feel was installed, as {@link FlatLaf#setup(javax.swing.LookAndFeel)}
     */
    public static boolean setup() {
        return FlatLaf.setup(new FlatLagoonSoftLaf());
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return "Lagoon Soft - Lagoon with plainly shaded scroll bars and tabs"; }
}
