package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;

/**
 * A classic light theme for FlatLaf: neutral grey, square corners, one-pixel dark frames, framed
 * card tabs, white tables with a grid and blue scroll bars with buttons. Layout and sizes are FlatLaf
 * Light's; colours and sizes live in {@code FlatLagoonLaf.properties}.
 *
 * <p>Switches an application can turn on in its own {@code FlatLagoonLaf.properties}
 * ({@link FlatLaf#registerCustomDefaultsSource(String)}), for a denser, more classic look:
 * {@code Lagoon.dialogFont} (fonts based on Dialog 12), {@code Lagoon.labelGap} and
 * {@code Lagoon.oddButtonSize} ({@link CompoundSize}), {@code Lagoon.raisedArrowButtons}
 * ({@link LagoonComboBoxUI}, {@link LagoonSpinnerUI}).
 *
 * <p>Install with {@link #setup()}, or {@code FlatLaf.setup(new FlatLagoonLaf())}.
 */
public class FlatLagoonLaf extends FlatLightLaf {

    private static final Font BASE_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    public static final String NAME = "Lagoon";

    /**
     * Installs the theme, as {@link FlatLaf#setup(javax.swing.LookAndFeel)} does.
     *
     * @return whether the look-and-feel was installed, as {@link FlatLaf#setup(javax.swing.LookAndFeel)}
     */
    public static boolean setup() {
        return FlatLaf.setup(new FlatLagoonLaf());
    }

    /** Takes the base font back out, so the next look-and-feel does not mistake it for the application's. */
    @Override
    public void uninitialize() {
        if (UIManager.get("defaultFont") == BASE_FONT) {
            UIManager.put("defaultFont", null);
        }
        super.uninitialize();
    }

    /**
     * {@code UIManager.get} also reads the installed look-and-feel's defaults, and every FlatLaf theme
     * puts a {@code defaultFont} there; that one is the previous theme's, not the application's.
     */
    private static boolean applicationSetDefaultFont() {
        Object font = UIManager.get("defaultFont");
        return font != null && font != UIManager.getLookAndFeelDefaults().get("defaultFont");
    }

    /**
     * {@code FlatLagoonLaf-addons.properties}, then the same file of each Lagoon subclass down to this one
     * ({@code FlatLagoonDarkLaf-addons.properties}...): keys of add-on components, applied after the add-ons' own.
     */
    @Override
    protected Properties getAdditionalDefaults() {
        Properties properties = new Properties();
        Properties inherited = super.getAdditionalDefaults();
        if (inherited != null) {
            properties.putAll(inherited);
        }
        Deque<Class<?>> lagoonClasses = new ArrayDeque<>();
        for (Class<?> c = getClass(); FlatLagoonLaf.class.isAssignableFrom(c); c = c.getSuperclass()) {
            lagoonClasses.push(c);
        }
        for (Class<?> c : lagoonClasses) {
            try (InputStream in = c.getResourceAsStream(c.getSimpleName() + "-addons.properties")) {
                if (in != null) {
                    properties.load(in);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return properties;
    }

    /**
     * With {@code Lagoon.dialogFont}, bases the fonts on Dialog 12, unless the application has put its own
     * {@code defaultFont} into {@link UIManager}. The font goes into {@code UIManager}, not the properties
     * file, so that FlatLaf still scales it. Decided here, after the previous look-and-feel is uninitialized,
     * so a font it put there and took back again is gone.
     */
    private static void chooseBaseFont(UIDefaults defaults) {
        if (Boolean.TRUE.equals(defaults.get("Lagoon.dialogFont")) && !applicationSetDefaultFont()) {
            UIManager.put("defaultFont", BASE_FONT);
        }
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults defaults = super.getDefaults();
        chooseBaseFont(defaults);
        // Frames the unselected tabs too, which no key can express.
        defaults.put("TabbedPaneUI", LagoonTabbedPaneUI.class.getName());
        // Label and button sizes keep the icon-text gap after the text (CompoundSize).
        defaults.put("LabelUI", LagoonLabelUI.class.getName());
        defaults.put("ButtonUI", LagoonButtonUI.class.getName());
        defaults.put("ToggleButtonUI", LagoonButtonUI.Toggle.class.getName());
        // The arrows of combo boxes and spinners are small raised buttons inside the frame.
        defaults.put("ComboBoxUI", LagoonComboBoxUI.class.getName());
        defaults.put("SpinnerUI", LagoonSpinnerUI.class.getName());
        // Toolbar separators keep their line where a toolbar is laid out by preferred sizes.
        defaults.put("ToolBarSeparatorUI", LagoonToolBarSeparatorUI.class.getName());
        // Raised arrow buttons, a framed thumb and a line towards the content.
        defaults.put("ScrollBarUI", LagoonScrollBarUI.class.getName());
        // A framed track with a framed bar inside it.
        defaults.put("ProgressBarUI", LagoonProgressBarUI.class.getName());
        // Boolean cells in the theme's check box; FlatLaf's table renderer has its own.
        defaults.put("TableUI", LagoonTableUI.class.getName());
        // A header the theme draws is as tall as its text.
        defaults.put("TableHeaderUI", LagoonTableHeaderUI.class.getName());
        // A tree asking for no lines (JTree.lineStyle "None") gets none.
        defaults.put("TreeUI", LagoonTreeUI.class.getName());
        // The corner above a table's vertical scroll bar, beside its header: plain ground. A new one per
        // table, which a lazy value would not give.
        defaults.put("Table.scrollPaneCornerComponent", (UIDefaults.ActiveValue) table -> new TableCorner());
        // Two-tone etched group border; a properties file cannot name a Border class.
        defaults.put("TitledBorder.border", new BorderUIResource.EtchedBorderUIResource(
                defaults.getColor("TitledBorder.highlightColor"), defaults.getColor("TitledBorder.shadowColor")));
        return defaults;
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return "Lagoon - a classic light theme for FlatLaf, with blue accents"; }

    /** See {@code Table.scrollPaneCornerComponent} in {@link #getDefaults()}. */
    private static final class TableCorner extends JComponent implements UIResource {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(UIManager.getColor("Panel.background"));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
