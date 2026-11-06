package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import java.util.ArrayList;
import java.util.List;

/**
 * Lagoon at night: the classic Lagoon's shapes, shading and sizes (raised buttons, framed card tabs, a table grid,
 * glossy scroll bars with buttons) in a calm blue-grey palette with soft text. Only the colours differ from
 * {@link FlatLagoonLaf}; they live in {@code FlatLagoonDarkLaf.properties}.
 *
 * <p>FlatLaf builds the defaults on FlatLaf Dark here, not FlatLaf Light: the properties files load in the
 * order FlatLaf, FlatLaf Dark, Lagoon, Lagoon Dark. An application's own {@code FlatLagoonLaf.properties}
 * ({@link FlatLaf#registerCustomDefaultsSource(String)}) applies to the dark themes too, so its colours there
 * take FlatLaf's {@code [light]} prefix, and dark ones go in its own {@code FlatLagoonDarkLaf.properties}.
 *
 * <p>Install with {@link #setup()}, or {@code FlatLaf.setup(new FlatLagoonDarkLaf())}.
 */
public class FlatLagoonDarkLaf extends FlatLagoonLaf {

    public static final String NAME = "Lagoon Dark";

    /**
     * Installs the theme, as {@link FlatLaf#setup(javax.swing.LookAndFeel)} does.
     *
     * @return whether the look-and-feel was installed, as {@link FlatLaf#setup(javax.swing.LookAndFeel)}
     */
    public static boolean setup() {
        return FlatLaf.setup(new FlatLagoonDarkLaf());
    }

    @Override
    public boolean isDark() { return true; }

    /** FlatLaf, FlatLaf Dark, then every Lagoon class from {@link FlatLagoonLaf} down to this one. */
    @Override
    protected List<Class<?>> getLafClassesForDefaultsLoading() {
        List<Class<?>> lagoon = new ArrayList<>();
        for (Class<?> c = getClass(); FlatLagoonLaf.class.isAssignableFrom(c); c = c.getSuperclass()) {
            lagoon.add(0, c);
        }
        List<Class<?>> classes = new ArrayList<>();
        classes.add(FlatLaf.class);
        classes.add(FlatDarkLaf.class);
        classes.addAll(lagoon);
        return classes;
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return "Lagoon Dark - the classic Lagoon in a calm blue-grey night palette"; }
}
