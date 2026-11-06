package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;

import java.util.List;

/**
 * {@link FlatLagoonDarkLaf} with the quieter Soft shade of {@link FlatLagoonSoftLaf}: the dark palette, scroll
 * thumbs and tabs shaded plainly, smaller round ends. Its defaults load {@code FlatLagoonSoftLaf.properties}
 * after the dark palette.
 *
 * <p>Install with {@link #setup()}, or {@code FlatLaf.setup(new FlatLagoonSoftDarkLaf())}.
 */
public class FlatLagoonSoftDarkLaf extends FlatLagoonDarkLaf {

    public static final String NAME = "Lagoon Soft Dark";

    /**
     * Installs the theme, as {@link FlatLaf#setup(javax.swing.LookAndFeel)} does.
     *
     * @return whether the look-and-feel was installed, as {@link FlatLaf#setup(javax.swing.LookAndFeel)}
     */
    public static boolean setup() {
        return FlatLaf.setup(new FlatLagoonSoftDarkLaf());
    }

    /** As {@link FlatLagoonDarkLaf}'s, with Soft's file before this class's own. */
    @Override
    protected List<Class<?>> getLafClassesForDefaultsLoading() {
        List<Class<?>> classes = super.getLafClassesForDefaultsLoading();
        classes.add(classes.indexOf(FlatLagoonSoftDarkLaf.class), FlatLagoonSoftLaf.class);
        return classes;
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return "Lagoon Soft Dark - Lagoon Dark with plainly shaded scroll bars and tabs"; }
}
