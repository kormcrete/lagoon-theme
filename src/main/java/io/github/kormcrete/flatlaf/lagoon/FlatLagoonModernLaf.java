package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.FontUtils;

import javax.swing.UIDefaults;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The modern Lagoon: a light ground ({@code #F4F7FA}) with white cards, soft rounded corners, one blue accent, teal
 * for what is good and amber for what needs attention. Colours, corner radii, control heights and text sizes come
 * from the Lagoon design tokens ({@link LagoonTokens}, the file the Compose theme is generated from too); how they
 * map onto FlatLaf lives in {@code FlatLagoonModernLaf.properties}.
 *
 * <p>The tokens become UI defaults: {@code Lagoon.color.<name>} (every colour of the tokens),
 * {@code Lagoon.tile.<i>.background} and {@code .foreground}, {@code Lagoon.radius.<name>} and
 * {@code Lagoon.arc.<name>} (twice the radius, as FlatLaf's {@code arc} keys take it), {@code Lagoon.metrics.<name>}
 * (the desktop lengths), {@code Lagoon.textSize.<name>} and the text styles {@code Lagoon.titleFont} and the like.
 * They are applied last, so an application re-tints through the theme's own keys ({@code @accentColor},
 * {@code Button.default.background}, the {@code [style]} rules...), not through these.
 *
 * <p>Text: the body text is the platform's UI font size, so FlatLaf scales as it does for its own themes; headings
 * and small text keep the tokens' steps from the body size ({@code h1}, {@code h2}, {@code h3}, {@code small},
 * {@code caption} style classes). The tokens name Inter: with {@code com.formdev:flatlaf-fonts-inter} on the
 * classpath (or Inter installed), the theme uses it, unless the application chose a family of its own with
 * {@link FlatLaf#setPreferredFontFamily(String)}; otherwise the platform's UI font.
 *
 * <p>Style classes ({@code FlatClientProperties.STYLE_CLASS}): {@code card}, {@code primary}, {@code tonal},
 * {@code danger}, {@code h1}, {@code h2}, {@code h3}, {@code small}, {@code caption}, {@code muted},
 * {@code pill-info}, {@code pill-success}, {@code pill-attention}, {@code pill-danger}, {@code pill-neutral},
 * {@code segmented} and {@code search}. Components: {@link LagoonCard}, {@link StatusPill}, {@link AvatarTile},
 * {@link Stepper}, {@link SegmentedControl}, {@link Scrim}.
 *
 * <p>Install with {@link #setup()}, or {@code FlatLaf.setup(new FlatLagoonModernLaf())}.
 */
public class FlatLagoonModernLaf extends FlatLightLaf {

    public static final String NAME = "Lagoon Modern";

    /** The font family this theme made FlatLaf prefer, to give back in {@link #uninitialize()}; null if none. */
    private String preferredFamily;

    /**
     * Installs the theme, as {@link FlatLaf#setup(javax.swing.LookAndFeel)} does.
     *
     * @return whether the look-and-feel was installed, as {@link FlatLaf#setup(javax.swing.LookAndFeel)}
     */
    public static boolean setup() {
        return FlatLaf.setup(new FlatLagoonModernLaf());
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return "Lagoon Modern - a light FlatLaf theme with white cards and soft corners"; }

    /** Picks the tokens' font family before FlatLaf builds its fonts; the previous look-and-feel is gone by now. */
    @Override
    public void initialize() {
        preferredFamily = preferFontFamily(LagoonTokens.get().fontFamily());
        super.initialize();
    }

    @Override
    public void uninitialize() {
        if (preferredFamily != null && preferredFamily.equals(FlatLaf.getPreferredFontFamily())) {
            FlatLaf.setPreferredFontFamily(null);
            FlatLaf.setPreferredLightFontFamily(null);
            FlatLaf.setPreferredSemiboldFontFamily(null);
        }
        preferredFamily = null;
        super.uninitialize();
    }

    /**
     * Makes FlatLaf prefer {@code family} (and its Light and SemiBold faces, where they exist), unless the
     * application already chose a family or the font is not there.
     *
     * @return the family now preferred, or null if nothing changed
     */
    static String preferFontFamily(String family) {
        if (FlatLaf.getPreferredFontFamily() != null || !fontAvailable(family)) {
            return null;
        }
        FlatLaf.setPreferredFontFamily(family);
        if (fontAvailable(family + " Light")) {
            FlatLaf.setPreferredLightFontFamily(family + " Light");
        }
        if (fontAvailable(family + " SemiBold")) {
            FlatLaf.setPreferredSemiboldFontFamily(family + " SemiBold");
        }
        return family;
    }

    /** Whether {@code family} can be shown: from flatlaf-fonts-inter (registered here) or installed. */
    static boolean fontAvailable(String family) {
        if (family.startsWith(BundledInter.FAMILY)) {
            BundledInter.register();
        }
        Font font = FontUtils.getCompositeFont(family, Font.PLAIN, 12);
        // Java falls back to Dialog for a family it does not know
        return !Font.DIALOG.equalsIgnoreCase(font.getFamily());
    }

    /** The tokens as UI defaults, applied after the properties files (see the class comment). */
    @Override
    protected Properties getAdditionalDefaults() {
        Properties properties = new Properties();
        Properties inherited = super.getAdditionalDefaults();
        if (inherited != null) {
            properties.putAll(inherited);
        }
        properties.putAll(tokenDefaults(LagoonTokens.get()));
        return properties;
    }

    /** The UI defaults made from {@code tokens}; see the class comment. */
    static Properties tokenDefaults(LagoonTokens tokens) {
        Properties p = new Properties();
        for (Map.Entry<String, Color> e : tokens.colors().entrySet()) {
            p.put("Lagoon.color." + e.getKey(), hex(e.getValue()));
        }
        List<Color[]> tiles = tokens.tiles();
        p.put("Lagoon.tileCount", String.valueOf(tiles.size()));
        for (int i = 0; i < tiles.size(); i++) {
            p.put("Lagoon.tile." + i + ".background", hex(tiles.get(i)[0]));
            p.put("Lagoon.tile." + i + ".foreground", hex(tiles.get(i)[1]));
        }
        for (Map.Entry<String, Integer> e : tokens.radii().entrySet()) {
            p.put("Lagoon.radius." + e.getKey(), String.valueOf(e.getValue()));
            p.put("Lagoon.arc." + e.getKey(), String.valueOf(2 * e.getValue()));
        }
        for (Map.Entry<String, Integer> e : tokens.lengths().entrySet()) {
            p.put("Lagoon.metrics." + e.getKey(), String.valueOf(e.getValue()));
        }
        for (Map.Entry<String, Integer> e : tokens.textSizes().entrySet()) {
            // "Size" ends a key FlatLaf reads as a dimension: titleSize -> Lagoon.textSize.title
            String name = e.getKey().endsWith("Size")
                    ? e.getKey().substring(0, e.getKey().length() - "Size".length()) : e.getKey();
            p.put("Lagoon.textSize." + name, String.valueOf(e.getValue()));
        }
        int gap = tokens.length("gap");
        int gutter = tokens.length("gutter");
        p.put("Lagoon.gapInsets", gap + "," + gap + "," + gap + "," + gap);
        p.put("Lagoon.gutterInsets", gutter + "," + gutter + "," + gutter + "," + gutter);

        // Text styles as in the Compose theme (LagoonTypography), each a step from the body size.
        int body = tokens.textSize("bodySize");
        p.put("Lagoon.titleFont", "bold " + step(tokens.textSize("titleSize") - body));
        p.put("Lagoon.headlineFont", "bold " + step(tokens.textSize("headlineSize") - body));
        p.put("Lagoon.headingFont", step(0) + " $semibold.font");
        p.put("Lagoon.smallFont", step(tokens.textSize("smallSize") - body));
        p.put("Lagoon.smallStrongFont", step(tokens.textSize("smallSize") - body) + " $semibold.font");
        p.put("Lagoon.labelFont", step(tokens.textSize("labelSize") - body) + " $semibold.font");
        p.put("Lagoon.fontFamily", '"' + tokens.fontFamily() + '"');

        // The card style class's frame: the gap inside, a hairline, the card's corners.
        p.put("Lagoon.cardBorder", gap + "," + gap + "," + gap + "," + gap + ","
                + hex(tokens.color("lineSoft")) + ",1," + 2 * tokens.radius("card"));
        return p;
    }

    private static String step(int size) {
        return size >= 0 ? "+" + size : String.valueOf(size);
    }

    /** FlatLaf's notation: {@code #RRGGBB}, or {@code #RRGGBBAA} with the alpha last. */
    static String hex(Color c) {
        String rgb = String.format("#%06X", c.getRGB() & 0xFFFFFF);
        return c.getAlpha() == 255 ? rgb : rgb + String.format("%02X", c.getAlpha());
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults defaults = super.getDefaults();
        // Buttons, fields, combo boxes and spinners as tall as the tokens' buttonHeight and controlHeight.
        defaults.put("ButtonUI", ModernControlUI.Button.class.getName());
        defaults.put("ToggleButtonUI", ModernControlUI.ToggleButton.class.getName());
        defaults.put("TextFieldUI", ModernControlUI.TextField.class.getName());
        defaults.put("PasswordFieldUI", ModernControlUI.PasswordField.class.getName());
        defaults.put("FormattedTextFieldUI", ModernControlUI.FormattedTextField.class.getName());
        defaults.put("ComboBoxUI", ModernControlUI.ComboBox.class.getName());
        defaults.put("SpinnerUI", ModernControlUI.Spinner.class.getName());
        return defaults;
    }

    /** Inter from flatlaf-fonts-inter, an optional dependency: touched only when the class is there. */
    private static final class BundledInter {
        static final String FAMILY = "Inter";
        private static final String CLASS = "com.formdev.flatlaf.fonts.inter.FlatInterFont";
        private static boolean registered;

        static synchronized void register() {
            if (registered) {
                return;
            }
            registered = true;
            try {
                Class.forName(CLASS, false, FlatLagoonModernLaf.class.getClassLoader());
            } catch (ClassNotFoundException | LinkageError e) {
                return;
            }
            Loader.installLazy();
        }

        /** Links FlatInterFont only when called. */
        private static final class Loader {
            static void installLazy() {
                com.formdev.flatlaf.fonts.inter.FlatInterFont.installLazy();
            }
        }
    }
}
