package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static io.github.kormcrete.flatlaf.lagoon.LagoonShadeTest.bar;
import static io.github.kormcrete.flatlaf.lagoon.LagoonShadeTest.str;
import static io.github.kormcrete.flatlaf.lagoon.LagoonShadeTest.tabColumn;
import static io.github.kormcrete.flatlaf.lagoon.LagoonShadeTest.thumbRow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The dark Lagoon themes, Gloss and Soft. Headless. */
class FlatLagoonDarkLafTest {

    private static final Pattern LITERAL_COLOUR = Pattern.compile("#[0-9A-Fa-f]{6}");

    @AfterEach
    void resetLookAndFeel() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
    }

    @Test
    void installsAsADarkLagoon() {
        assertTrue(FlatLagoonDarkLaf.setup());
        assertTrue(UIManager.getLookAndFeel() instanceof FlatLagoonLaf);
        assertEquals("Lagoon Dark", UIManager.getLookAndFeel().getName());
        assertTrue(FlatLaf.isLafDark());
        assertEquals("gloss", UIManager.getString("Lagoon.shade"));
        // Lagoon's own delegates and borders, as in the light theme.
        assertEquals(LagoonTabbedPaneUI.class.getName(), UIManager.getString("TabbedPaneUI"));
        assertEquals(LagoonScrollBarUI.class.getName(), UIManager.getString("ScrollBarUI"));
        assertEquals(LagoonBorders.Text.class, UIManager.getBorder("TextField.border").getClass());
        assertEquals(LagoonBorders.Button.class, UIManager.getBorder("Button.border").getClass());
    }

    @Test
    void softDarkIsTheDarkPaletteWithTheSoftShade() {
        assertTrue(FlatLagoonSoftDarkLaf.setup());
        assertEquals("Lagoon Soft Dark", UIManager.getLookAndFeel().getName());
        assertTrue(FlatLaf.isLafDark());
        assertEquals("soft", UIManager.getString("Lagoon.shade"));
        assertEquals(4, UIManager.getInt("ScrollBar.thumbArc"));
        assertEquals(4, UIManager.getInt("TabbedPane.cardTabArc"));
        Color dark = UIManager.getColor("Panel.background");
        FlatLagoonDarkLaf.setup();
        assertEquals(dark, UIManager.getColor("Panel.background"));
    }

    /** Every colour the light file writes out is set again for the dark themes, so none is left behind. */
    @Test
    void everyColourOfTheLightThemeIsSetAgain() throws IOException {
        for (String file : new String[]{"FlatLagoonLaf", "FlatLagoonLaf-addons"}) {
            Properties light = load(file + ".properties");
            Properties dark = load(file.replace("FlatLagoonLaf", "FlatLagoonDarkLaf") + ".properties");
            List<String> missing = new ArrayList<>();
            for (String key : light.stringPropertyNames()) {
                if (LITERAL_COLOUR.matcher(light.getProperty(key)).find() && !dark.containsKey(key)) {
                    missing.add(key);
                }
            }
            Collections.sort(missing);
            assertEquals(Collections.emptyList(), missing, file);
        }
    }

    /** The dark palette's colours are all variables, so a palette is just its first section. */
    @Test
    void theDarkFileWritesColoursOnlyInItsPalette() throws IOException {
        Properties dark = load("FlatLagoonDarkLaf.properties");
        for (String key : dark.stringPropertyNames()) {
            if (!key.startsWith("@")) {
                assertTrue(!LITERAL_COLOUR.matcher(dark.getProperty(key)).find(), key + " = " + dark.getProperty(key));
            }
        }
    }

    @Test
    void groundsAreDarkAndTextIsSoftAndReadable() {
        for (Supplier<Boolean> setup : themes()) {
            setup.get();
            for (String key : new String[]{"Panel.background", "Table.background", "List.background", "Tree.background",
                    "TextField.background", "ComboBox.popupBackground", "Viewport.background", "PopupMenu.background",
                    "MenuBar.background", "TableHeader.background", "ToolTip.background", "Lagoon.sheen.top",
                    "Lagoon.sheen.bottom", "Desktop.background", "ProgressBar.background"}) {
                assertTrue(luminance(UIManager.getColor(key)) < .1, key + " dark");
            }
            for (String[] pair : new String[][]{{"Label.foreground", "Panel.background"},
                    {"Table.foreground", "Table.background"}, {"TextField.foreground", "TextField.background"},
                    {"TableHeader.foreground", "TableHeader.background"}, {"MenuItem.foreground", "PopupMenu.background"},
                    {"Menu.foreground", "MenuBar.background"}, {"ToolTip.foreground", "ToolTip.background"},
                    {"Table.selectionInactiveForeground", "Table.selectionInactiveBackground"},
                    {"MenuItem.selectionForeground", "MenuItem.selectionBackground"},
                    {"TextComponent.selectionForeground", "TextComponent.selectionBackground"}}) {
                Color text = UIManager.getColor(pair[0]);
                // Soft: about half of white's luminance (FlatLaf Dark's text has .72)...
                assertTrue(luminance(text) < .55, pair[0] + " not glaring: " + text);
                // ...but readable (WCAG AA for normal text).
                assertTrue(contrast(text, UIManager.getColor(pair[1])) >= 4.5,
                        pair[0] + " on " + pair[1] + ": " + contrast(text, UIManager.getColor(pair[1])));
            }
            // Only a focused selection, on the strong accent, takes brighter text, and stays readable there.
            assertTrue(contrast(UIManager.getColor("Table.selectionForeground"),
                    UIManager.getColor("Table.selectionBackground")) >= 4.5);
            // Disabled text stays legible, if quieter.
            assertTrue(contrast(UIManager.getColor("Label.disabledForeground"), UIManager.getColor("Panel.background")) >= 2.5);
            // Frames stand out from the ground they sit on.
            assertTrue(contrast(UIManager.getColor("Component.borderColor"), UIManager.getColor("Panel.background")) >= 2);
            assertTrue(contrast(UIManager.getColor("Component.borderColor"), UIManager.getColor("TextField.background")) >= 2);
        }
    }

    /** Dark changes colours only: every size, inset, arc and switch is the light theme's. */
    @Test
    void sizesAndSwitchesAreTheLightThemes() {
        Map<String, Object> light = nonColours(FlatLagoonLaf::setup);
        Map<String, Object> dark = nonColours(FlatLagoonDarkLaf::setup);
        assertEquals(light, dark);
        assertEquals(nonColours(FlatLagoonSoftLaf::setup), nonColours(FlatLagoonSoftDarkLaf::setup));
    }

    @Test
    void componentsAreAsLargeAsInTheLightTheme() {
        List<Dimension> light = preferredSizes(FlatLagoonLaf::setup);
        assertEquals(light, preferredSizes(FlatLagoonDarkLaf::setup));
        assertEquals(light, preferredSizes(FlatLagoonSoftDarkLaf::setup));
    }

    /** The group border is put together in Java; switching keeps it in step with the palette. */
    @Test
    void theGroupBorderFollowsTheThemeBackAndForth() {
        FlatLagoonDarkLaf.setup();
        EtchedBorder dark = (EtchedBorder) UIManager.getBorder("TitledBorder.border");
        assertEquals(UIManager.getColor("Component.ringHighlightColor"), dark.getHighlightColor());
        assertEquals(UIManager.getColor("Component.ringShadowColor"), dark.getShadowColor());
        FlatLagoonLaf.setup();
        EtchedBorder light = (EtchedBorder) UIManager.getBorder("TitledBorder.border");
        assertEquals(new Color(0xF7F8F8), light.getHighlightColor());
        assertEquals(new Color(0xBCBDBE), light.getShadowColor());
    }

    @Test
    void anAddOnsTaskPaneTakesTheDarkPalette() {
        FlatLagoonDarkLaf.setup();
        assertEquals(UIManager.getColor("ToolTip.background"), UIManager.getColor("TaskPane.specialTitleBackground"));
        FlatLagoonLaf.setup();
        assertEquals(new Color(0xB2D8FF), UIManager.getColor("TaskPane.specialTitleBackground"));
    }

    /** The gloss still reads on dark: a tube thumb and a pillow tab, compared within one picture. */
    @Test
    void theGlossShadingReadsOnDark() {
        FlatLagoonDarkLaf.setup();
        float[] row = thumbRow(bar(), false);
        assertTrue(row[3] > row[8] + 8, "leading side lighter than the middle " + str(row));
        assertTrue(row[12] > row[8] + 8, "glow lighter than the middle " + str(row));
        JScrollBar bar = bar();
        float[] resting = thumbRow(bar, false);
        float[] hovered = thumbRow(bar, true);
        assertNotEquals(str(resting), str(hovered), "the mouse on the bar shows on the thumb");
        assertTrue(hovered[3] > hovered[8] && hovered[12] > hovered[8], "still a tube " + str(hovered));
        float[] column = tabColumn();
        int n = column.length;
        assertTrue(column[1] > column[n * 55 / 100] + 2, str(column));
    }

    @Test
    void theSoftShadingReadsOnDark() {
        FlatLagoonSoftDarkLaf.setup();
        float[] row = thumbRow(bar(), false);
        assertTrue(row[3] > row[8] && row[8] > row[13], "lighter to darker " + str(row));
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static List<Supplier<Boolean>> themes() {
        List<Supplier<Boolean>> themes = new ArrayList<>();
        themes.add(FlatLagoonDarkLaf::setup);
        themes.add(FlatLagoonSoftDarkLaf::setup);
        return themes;
    }

    private static Properties load(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = FlatLagoonLaf.class.getResourceAsStream(name)) {
            properties.load(in);
        }
        return properties;
    }

    /** Every default that is not a colour, nor depends on one, nor is FlatLaf's own dark tuning. */
    private static Map<String, Object> nonColours(Supplier<Boolean> setup) {
        setup.get();
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        Map<String, Object> values = new TreeMap<>();
        for (Object key : Collections.list(defaults.keys())) {
            if (!(key instanceof String)) {
                continue;
            }
            String name = (String) key;
            Object value = defaults.get(key);
            boolean comparable = value instanceof Number || value instanceof Boolean || value instanceof Insets
                    || value instanceof Dimension || (value instanceof String && !name.startsWith("[style]"));
            if (comparable && !name.equals("laf.dark") && !name.contains("DropShadowOpacity")
                    && !name.equals("Popup.dropShadowOpacity") && !name.contains("grayFilter")) {
                values.put(name, value);
            }
        }
        return values;
    }

    private static List<Dimension> preferredSizes(Supplier<Boolean> setup) {
        setup.get();
        JComponent[] components = {new JButton("Save"), new JTextField(12), new JComboBox<>(new String[]{"All"}),
                new JSpinner(), new JTable(3, 3).getTableHeader(), new JScrollBar(), tabs()};
        List<Dimension> sizes = new ArrayList<>();
        for (JComponent c : components) {
            sizes.add(c.getPreferredSize());
        }
        return sizes;
    }

    private static JTabbedPane tabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Orders", new JButton("x"));
        return tabs;
    }

    /** WCAG relative luminance. */
    private static double luminance(Color c) {
        return .2126 * channel(c.getRed()) + .7152 * channel(c.getGreen()) + .0722 * channel(c.getBlue());
    }

    private static double channel(int v) {
        double s = v / 255.0;
        return s <= .03928 ? s / 12.92 : Math.pow((s + .055) / 1.055, 2.4);
    }

    private static double contrast(Color a, Color b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + .05) / (Math.min(la, lb) + .05);
    }
}
