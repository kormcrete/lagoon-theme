package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.UIScale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Headless: installing the look-and-feel and painting components off screen needs no display. */
class FlatLagoonModernLafTest {

    private static final LagoonTokens TOKENS = LagoonTokens.parse(tokensFile());

    /** What FlatLaf logs as a problem: a value it could not parse, a style it could not apply. */
    private final List<String> problems = new ArrayList<>();
    private final Handler logged = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                problems.add(record.getMessage() + (record.getThrown() != null ? ": " + record.getThrown() : ""));
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void listenToFlatLaf() {
        Logger.getLogger(FlatLaf.class.getName()).addHandler(logged);
    }

    @AfterEach
    void resetLookAndFeel() throws Exception {
        Logger.getLogger(FlatLaf.class.getName()).removeHandler(logged);
        UIManager.put("defaultFont", null);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        FlatLaf.setPreferredFontFamily(null);
        FlatLaf.setPreferredLightFontFamily(null);
        FlatLaf.setPreferredSemiboldFontFamily(null);
    }

    /** The project's tokens file, which the Compose theme is generated from (tests run in the project folder). */
    private static String tokensFile() {
        try {
            return new String(Files.readAllBytes(Paths.get("tokens", "lagoon.tokens.json")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("run the tests in the project folder", e);
        }
    }

    private static int scale(int unscaled) {
        return UIScale.scale(unscaled);
    }

    private static <T extends JComponent> T styled(T c, String styleClass) {
        c.putClientProperty(FlatClientProperties.STYLE_CLASS, styleClass);
        return c;
    }

    // ---------------------------------------------------------------- tokens

    @Test
    void installsAsTheCurrentLookAndFeel() {
        assertTrue(FlatLagoonModernLaf.setup());
        assertEquals(FlatLagoonModernLaf.class, UIManager.getLookAndFeel().getClass());
        assertEquals("Lagoon Modern", UIManager.getLookAndFeel().getName());
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void theJarCarriesTheProjectsTokensFile() throws IOException {
        try (InputStream in = LagoonTokens.class.getResourceAsStream(LagoonTokens.RESOURCE)) {
            assertNotNull(in, "the tokens file is not on the classpath");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = in.read(buffer)) > 0; ) {
                bytes.write(buffer, 0, n);
            }
            assertEquals(tokensFile(), new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    /** Every "name": "#colour" of the file, found by a pattern rather than the parser, is the parser's colour. */
    @Test
    void readsEveryColourOfTheFile() {
        String file = tokensFile();
        String colours = file.substring(file.indexOf("\"color\""), file.indexOf("\"tiles\""));
        Matcher m = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})\"").matcher(colours);
        int found = 0;
        while (m.find()) {
            String hex = m.group(2);
            long argb = Long.parseLong(hex.length() == 6 ? "FF" + hex : hex, 16);
            assertEquals(new Color((int) argb, true), TOKENS.color(m.group(1)), m.group(1));
            found++;
        }
        assertEquals(TOKENS.colors().size(), found);
        assertEquals(new Color(0xF4F7FA), TOKENS.color("ground"));
        assertEquals(0x6B, TOKENS.color("scrim").getAlpha(), "#AARRGGBB: the alpha comes first");
        assertEquals(6, TOKENS.tiles().size());
        assertEquals(16, TOKENS.radius("card"));
        assertEquals(36, TOKENS.length("controlHeight"));
        assertEquals(14, TOKENS.textSize("bodySize"));
        assertEquals("Inter", TOKENS.fontFamily());
    }

    @Test
    void theParserReadsJsonAndRefusesWhatIsNot() {
        Object doc = new LagoonTokens.Json("{\"a\": [1, -2.5e1, true, null, \"x\\\"\\u00e9\\n\"], \"b\": {}}").document();
        assertEquals("{a=[1, -25.0, true, null, x\"é\n], b={}}", doc.toString());
        for (String bad : new String[] {"{", "{\"a\" 1}", "[1,]", "{\"a\": tru}", "\"open", "{} x"}) {
            assertThrows(IllegalArgumentException.class, () -> new LagoonTokens.Json(bad).document(), bad);
        }
    }

    @Test
    void everyTokenIsAUIDefault() {
        FlatLagoonModernLaf.setup();
        for (Map.Entry<String, Color> e : TOKENS.colors().entrySet()) {
            Color actual = UIManager.getColor("Lagoon.color." + e.getKey());
            assertEquals(e.getValue().getRGB(), actual.getRGB(), e.getKey());
            assertEquals(e.getValue().getAlpha(), actual.getAlpha(), e.getKey());
        }
        assertEquals(TOKENS.tiles().size(), UIManager.getInt("Lagoon.tileCount"));
        for (int i = 0; i < TOKENS.tiles().size(); i++) {
            assertEquals(TOKENS.tiles().get(i)[0], UIManager.getColor("Lagoon.tile." + i + ".background"));
            assertEquals(TOKENS.tiles().get(i)[1], UIManager.getColor("Lagoon.tile." + i + ".foreground"));
        }
        for (Map.Entry<String, Integer> e : TOKENS.radii().entrySet()) {
            assertEquals((int) e.getValue(), UIManager.getInt("Lagoon.radius." + e.getKey()), e.getKey());
            assertEquals(2 * e.getValue(), UIManager.getInt("Lagoon.arc." + e.getKey()), e.getKey());
        }
        for (Map.Entry<String, Integer> e : TOKENS.lengths().entrySet()) {
            assertEquals((int) e.getValue(), UIManager.getInt("Lagoon.metrics." + e.getKey()), e.getKey());
        }
        assertEquals(TOKENS.textSize("titleSize"), UIManager.getInt("Lagoon.textSize.title"));
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void theThemeIsMadeOfTheTokens() {
        FlatLagoonModernLaf.setup();
        assertEquals(TOKENS.color("ground"), UIManager.getColor("Panel.background"));
        assertEquals(TOKENS.color("ink"), UIManager.getColor("Label.foreground"));
        assertEquals(TOKENS.color("surface"), UIManager.getColor("TextField.background"));
        assertEquals(TOKENS.color("line"), UIManager.getColor("Component.borderColor"));
        assertEquals(TOKENS.color("primary"), UIManager.getColor("Button.default.background"));
        assertEquals(TOKENS.color("primary"), UIManager.getColor("Component.accentColor"));
        assertEquals(TOKENS.color("primary"), UIManager.getColor("Table.selectionBackground"));
        assertEquals(TOKENS.color("selection"), UIManager.getColor("Table.selectionInactiveBackground"));
        assertEquals(2 * TOKENS.radius("control"), UIManager.getInt("Component.arc"));
        assertEquals(2 * TOKENS.radius("control"), UIManager.getInt("Button.arc"));
        assertEquals(2 * TOKENS.radius("control"), UIManager.getInt("TextComponent.arc"));
        assertEquals(2 * TOKENS.radius("card"), UIManager.getInt("LagoonCard.arc"));
        assertEquals(TOKENS.length("rowHeight"), UIManager.getInt("Table.rowHeight"));
        assertEquals(TOKENS.length("controlHeight"), UIManager.getInt("TableHeader.height"));
        int gap = TOKENS.length("gap");
        assertEquals(new Insets(gap, gap, gap, gap), UIManager.getInsets("LagoonCard.padding"));
        Color scrim = UIManager.getColor("Scrim.background");
        assertEquals(TOKENS.color("scrim").getRGB(), scrim.getRGB());
        for (LagoonTone tone : LagoonTone.values()) {
            assertNotNull(tone.background(), tone.key());
            assertNotNull(tone.foreground(), tone.key());
        }
        assertEquals(TOKENS.color("successTint"), LagoonTone.SUCCESS.background());
        assertEquals(TOKENS.color("attention"), LagoonTone.ATTENTION.foreground());
        assertEquals(Arrays.asList(), problems);
    }

    // ---------------------------------------------------------------- fonts

    @Test
    void usesTheTokensFontFamilyWhenItIsThere() {
        FlatLagoonModernLaf.setup();
        assertEquals(TOKENS.fontFamily(), UIManager.getFont("defaultFont").getFamily());
        assertEquals(TOKENS.fontFamily(), UIManager.getFont("Label.font").getFamily());
        assertEquals(TOKENS.fontFamily() + " SemiBold", UIManager.getFont("Lagoon.labelFont").getFamily());
    }

    @Test
    void givesThePreferredFamilyBackToTheNextLookAndFeel() {
        FlatLagoonModernLaf.setup();
        assertEquals(TOKENS.fontFamily(), FlatLaf.getPreferredFontFamily());
        FlatLightLaf.setup();
        assertNull(FlatLaf.getPreferredFontFamily());
        assertNull(FlatLaf.getPreferredSemiboldFontFamily());
    }

    @Test
    void leavesAnApplicationsFamilyAlone() {
        FlatLaf.setPreferredFontFamily(Font.SANS_SERIF);
        FlatLagoonModernLaf.setup();
        assertEquals(Font.SANS_SERIF, FlatLaf.getPreferredFontFamily());
        FlatLightLaf.setup();
        assertEquals(Font.SANS_SERIF, FlatLaf.getPreferredFontFamily());
    }

    @Test
    void fallsBackToThePlatformFontWithoutTheFamily() {
        assertFalse(FlatLagoonModernLaf.fontAvailable("No Such Family Lagoon"));
        assertNull(FlatLagoonModernLaf.preferFontFamily("No Such Family Lagoon"));
        assertNull(FlatLaf.getPreferredFontFamily());
    }

    /** The body is the platform's size, so FlatLaf scales as it does; the rest keep the tokens' steps from it. */
    @Test
    void textStylesKeepTheTokensStepsFromTheBody() {
        FlatLagoonModernLaf.setup();
        int body = UIManager.getFont("defaultFont").getSize();
        int bodyToken = TOKENS.textSize("bodySize");
        Font h1 = styled(new JLabel("Orders"), "h1").getFont();
        assertEquals(body + scale(TOKENS.textSize("titleSize") - bodyToken), h1.getSize());
        assertTrue(h1.isBold());
        Font h2 = styled(new JLabel("Orders"), "h2").getFont();
        assertEquals(body + scale(TOKENS.textSize("headlineSize") - bodyToken), h2.getSize());
        assertEquals(body, styled(new JLabel("Orders"), "h3").getFont().getSize());
        assertEquals(body + scale(TOKENS.textSize("smallSize") - bodyToken),
                styled(new JLabel("note"), "small").getFont().getSize());
        Font caption = styled(new JLabel("DELIVERY"), "caption").getFont();
        assertEquals(body + scale(TOKENS.textSize("labelSize") - bodyToken), caption.getSize());
        assertEquals(UIManager.getFont("semibold.font").getFamily(), caption.getFamily());
        assertEquals(Arrays.asList(), problems);
    }

    // ---------------------------------------------------------------- style classes

    @Test
    void buttonStyleClassesTakeTheirColours() {
        FlatLagoonModernLaf.setup();
        JButton primary = styled(new JButton("New order"), "primary");
        assertEquals(TOKENS.color("primary"), primary.getBackground());
        assertEquals(TOKENS.color("onPrimary"), primary.getForeground());
        assertEquals(TOKENS.color("primaryStrong"), FlatLaf.getStyleableValue(primary, "hoverBackground"));
        JButton tonal = styled(new JButton("Export"), "tonal");
        assertEquals(TOKENS.color("primaryTint"), tonal.getBackground());
        assertEquals(TOKENS.color("primaryStrong"), tonal.getForeground());
        JButton danger = styled(new JButton("Delete"), "danger");
        assertEquals(TOKENS.color("surface"), danger.getBackground());
        assertEquals(TOKENS.color("danger"), danger.getForeground());
        assertEquals(TOKENS.color("dangerLine"), FlatLaf.getStyleableValue(danger, "borderColor"));
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void textStyleClassesAndCardsResolve() {
        FlatLagoonModernLaf.setup();
        assertEquals(TOKENS.color("inkMuted"), styled(new JLabel("note"), "muted").getForeground());
        JLabel both = styled(new JLabel("Orders"), "h2 muted");
        assertEquals(TOKENS.color("inkMuted"), both.getForeground());
        assertTrue(both.getFont().isBold());

        JPanel card = styled(new JPanel(), "card");
        assertEquals(TOKENS.color("surface"), card.getBackground());
        int gap = scale(TOKENS.length("gap"));
        assertEquals(new Insets(gap, gap, gap, gap), card.getInsets());
        assertEquals((Object) (2 * TOKENS.radius("card")), FlatLaf.getStyleableValue(card, "arc"));

        JTextField search = styled(new JTextField(), "search");
        assertNotNull(FlatLaf.getStyleableValue(search, "leadingIcon"));
        assertEquals(true, FlatLaf.getStyleableValue(search, "showClearButton"));

        JToggleButton segment = styled(new JToggleButton("All"), "segmented");
        assertEquals(TOKENS.color("surface"), FlatLaf.getStyleableValue(segment, "selectedBackground"));
        assertEquals(TOKENS.color("track"), segment.getBackground());
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void everyToneHasAPillStyleClass() {
        FlatLagoonModernLaf.setup();
        for (LagoonTone tone : LagoonTone.values()) {
            assertNotNull(UIManager.get("[style]Label." + tone.pillStyleClass()), tone.key());
            JLabel pill = styled(new JLabel("Placed"), tone.pillStyleClass());
            assertEquals(tone.background(), pill.getBackground(), tone.key());
            assertEquals(tone.foreground(), pill.getForeground(), tone.key());
        }
        assertEquals(Arrays.asList(), problems);
    }

    // ---------------------------------------------------------------- heights

    /**
     * A control is the tokens' height, or its own if its text needs more: the same control with
     * {@code JComponent.minimumHeight} 0 shows the height its text and padding take.
     */
    private static void assertTokenHeight(int token, Supplier<JComponent> control) {
        JComponent c = control.get();
        JComponent free = control.get();
        free.putClientProperty(FlatClientProperties.MINIMUM_HEIGHT, 0);
        int expected = Math.max(scale(token), free.getPreferredSize().height);
        assertEquals(expected, c.getPreferredSize().height, c.getClass().getSimpleName());
        assertTrue(free.getPreferredSize().height < scale(token),
                c.getClass().getSimpleName() + " was already that tall; the test proves nothing");
    }

    @Test
    void controlsAreAsTallAsTheTokensSay() {
        FlatLagoonModernLaf.setup();
        int control = TOKENS.length("controlHeight");
        // One height for every control: buttons too.
        assertTokenHeight(control, () -> new JButton("Save"));
        assertTokenHeight(control, () -> new JToggleButton("Filter"));
        assertTokenHeight(control, () -> new JTextField(10));
        assertTokenHeight(control, () -> new JPasswordField(10));
        assertTokenHeight(control, () -> new JFormattedTextField("1"));
        assertTokenHeight(control, () -> new JComboBox<>(new String[] {"Standard"}));
        assertTokenHeight(control, () -> new JSpinner());
        assertEquals(Arrays.asList(), problems);
    }

    /** A button showing only an icon is a square as tall as the other controls. */
    @Test
    void anIconOnlyButtonIsASquareAsTallAsTheOtherControls() {
        FlatLagoonModernLaf.setup();
        JButton button = new JButton(new javax.swing.ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)));
        Dimension size = button.getPreferredSize();
        assertEquals(new JTextField(10).getPreferredSize().height, size.height);
        assertEquals(size.height, size.width);
    }

    /** Rows are one height whatever shows them: tables, trees and lists. */
    @Test
    void treeAndListRowsAreAsTallAsTableRows() {
        FlatLagoonModernLaf.setup();
        int row = scale(TOKENS.length("rowHeight"));
        assertEquals(row, new javax.swing.JTable(3, 2).getRowHeight());
        javax.swing.JTree tree = new javax.swing.JTree();
        assertEquals(row, tree.getRowBounds(0).height);
        javax.swing.JList<String> list = new javax.swing.JList<>(new String[] {"Office", "Coffee"});
        list.setSize(200, 400);
        assertEquals(row, list.getCellBounds(0, 0).height);
        // A renderer taller than the row keeps its height in a list.
        javax.swing.JList<String> tall = new javax.swing.JList<>(new String[] {"<html>One<br>Two<br>Three<br>Four</html>"});
        tall.setSize(200, 400);
        assertTrue(tall.getCellBounds(0, 0).height > row);
    }

    @Test
    void controlsInsideOtherControlsAndToolbarsKeepTheirHeight() {
        FlatLagoonModernLaf.setup();
        JSpinner spinner = new JSpinner();
        JComponent editorField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        assertTrue(editorField.getPreferredSize().height < scale(TOKENS.length("controlHeight")));
        JToolBar bar = new JToolBar();
        JButton tool = new JButton("Save");
        bar.add(tool);
        assertTrue(tool.getPreferredSize().height < scale(TOKENS.length("controlHeight")));
    }

    // ---------------------------------------------------------------- components

    private static BufferedImage paint(JComponent c) {
        c.setSize(c.getPreferredSize());
        LagoonModernGallery.layOut(c);
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
        return image;
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    private static int brightness(int rgb) {
        return ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
    }

    @Test
    void aCardIsAWhiteFaceOnASoftShadowOnTheGround() {
        FlatLagoonModernLaf.setup();
        JPanel ground = new JPanel(null);
        LagoonCard card = new LagoonCard();
        JLabel content = new JLabel("Ada Shop Ltd");
        card.add(content);
        Insets shadow = LagoonCard.shadowInsets();
        int gap = scale(TOKENS.length("gap"));
        Dimension text = content.getPreferredSize();
        assertEquals(new Dimension(text.width + 2 * gap + shadow.left + shadow.right,
                text.height + 2 * gap + shadow.top + shadow.bottom), card.getPreferredSize());

        card.setPreferredSize(new Dimension(200, 120));
        card.setBounds(0, 0, 200, 120);
        ground.add(card);
        ground.setPreferredSize(new Dimension(200, 120));
        BufferedImage image = paint(ground);
        int groundRgb = TOKENS.color("ground").getRGB() & 0xFFFFFF;
        int surface = TOKENS.color("surface").getRGB() & 0xFFFFFF;
        int faceBottom = 120 - shadow.bottom;
        // the face, between the text and the edge
        assertEquals(surface, rgb(image, 100, faceBottom - 3));
        // the rounded corner shows what is under the card, not the face
        assertNotEquals(surface, rgb(image, shadow.left, shadow.top));
        // just under the face the shadow darkens the ground; it fades out towards the card's edge
        int under = rgb(image, 100, faceBottom + 1);
        int outer = rgb(image, 100, 119);
        assertTrue(brightness(under) < brightness(groundRgb), "no shadow under the card");
        assertTrue(brightness(under) < brightness(outer), "the shadow does not fade");
        assertEquals(groundRgb, rgb(image, 0, 0), "the corner of the bounds is the ground");
    }

    @Test
    void aStatusPillIsItsTextInAToneTint() {
        FlatLagoonModernLaf.setup();
        StatusPill pill = new StatusPill("Placed", LagoonTone.SUCCESS);
        FontMetrics fm = pill.getFontMetrics(pill.getFont());
        int dot = scale(UIManager.getInt("StatusPill.dotWidth"));
        Dimension size = pill.getPreferredSize();
        assertEquals(Math.max(fm.getHeight(), dot) + scale(3) + scale(3), size.height);
        // FlatLaf scales a label's icon-text gap when it lays the label out
        assertEquals(5, pill.getIconTextGap());
        assertEquals(scale(7) + dot + scale(5) + fm.stringWidth("Placed") + scale(8), size.width);

        BufferedImage image = paint(pill);
        int tint = LagoonTone.SUCCESS.background().getRGB();
        // in the right padding, at mid height: the tint; the corner: nothing, the pill is round
        assertEquals(tint, image.getRGB(size.width - scale(4), size.height / 2));
        assertEquals(0, image.getRGB(0, 0) >>> 24);

        pill.setTone(LagoonTone.DANGER);
        assertEquals(LagoonTone.DANGER.background(), pill.getBackground());
        assertEquals(LagoonTone.DANGER.foreground(), pill.getForeground());
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void anAvatarTileTakesTheSameColoursForTheSameKey() {
        FlatLagoonModernLaf.setup();
        String key = "Ada Shop Ltd";
        int index = (key.hashCode() & Integer.MAX_VALUE) % TOKENS.tiles().size();
        assertEquals(index, AvatarTile.tileIndex(key, TOKENS.tiles().size()));
        AvatarTile tile = new AvatarTile("AS", key);
        assertEquals(TOKENS.tiles().get(index)[0], tile.getTileBackground());
        assertEquals(TOKENS.tiles().get(index)[1], tile.getTileForeground());
        assertEquals(new AvatarTile("A", key).getTileBackground(), tile.getTileBackground());
        assertEquals(new Dimension(scale(40), scale(40)), tile.getPreferredSize());

        BufferedImage image = paint(tile);
        assertEquals(tile.getTileBackground().getRGB(), image.getRGB(scale(20), scale(2)));
        assertEquals(0, image.getRGB(0, 0) >>> 24, "the corners are rounded");

        tile.setTone(LagoonTone.ATTENTION);
        assertEquals(LagoonTone.ATTENTION.background(), tile.getTileBackground());
    }

    @Test
    void aStepperStepsBetweenItsBounds() {
        FlatLagoonModernLaf.setup();
        Stepper stepper = new Stepper(1, 0, 3);
        AtomicInteger changes = new AtomicInteger();
        stepper.addChangeListener(e -> changes.incrementAndGet());
        stepper.getDecrementButton().doClick();
        assertEquals(0, stepper.getValue());
        assertFalse(stepper.getDecrementButton().isEnabled());
        for (int i = 0; i < 5; i++) {
            stepper.getIncrementButton().doClick();
        }
        assertEquals(3, stepper.getValue());
        assertFalse(stepper.getIncrementButton().isEnabled());
        assertEquals(4, changes.get(), "clicks on a disabled button change nothing");
        stepper.setValue(-7);
        assertEquals(0, stepper.getValue());
        assertEquals("0", stepper.getValueLabel().getText());
    }

    @Test
    void aStepperIsAPillTheTouchTargetTall() {
        FlatLagoonModernLaf.setup();
        Stepper stepper = new Stepper(3, 0, 99);
        Dimension size = stepper.getPreferredSize();
        int height = Math.max(scale(TOKENS.length("touchTarget")), stepper.getValueLabel().getPreferredSize().height);
        assertEquals(height, size.height);
        FontMetrics fm = stepper.getValueLabel().getFontMetrics(stepper.getValueLabel().getFont());
        assertTrue(size.width >= 2 * Math.round(height * 0.95f) + fm.stringWidth("99"), "room for 99");

        BufferedImage plain = paint(stepper);
        assertEquals(UIManager.getColor("Stepper.background").getRGB(), plain.getRGB(size.width / 2, 1));
        stepper.setFilled(true);
        BufferedImage filled = paint(stepper);
        assertEquals(TOKENS.color("primary").getRGB(), filled.getRGB(size.width / 2, 1));
        assertEquals(0, filled.getRGB(0, 0) >>> 24, "the ends are round");
        assertEquals(TOKENS.color("onPrimary"), stepper.getValueLabel().getForeground());
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void aSegmentedControlIsAControlTallAndSelectsOneOption() {
        FlatLagoonModernLaf.setup();
        SegmentedControl control = new SegmentedControl("All 12", "Placed 4", "Shipped 6");
        assertEquals(0, control.getSelectedIndex());
        Insets track = control.getInsets();
        assertEquals(new Insets(scale(4), scale(4), scale(4), scale(4)), track);
        assertEquals(TOKENS.color("track"), control.getBackground());

        JToggleButton free = styled(new JToggleButton("Placed 4"), "segmented");
        free.setFont(control.getSegment(1).getFont());
        free.putClientProperty(FlatClientProperties.MINIMUM_HEIGHT, 0);
        int segment = Math.max(scale(TOKENS.length("controlHeight")) - track.top - track.bottom,
                free.getPreferredSize().height);
        assertEquals(track.top + segment + track.bottom, control.getPreferredSize().height);

        AtomicInteger changes = new AtomicInteger();
        control.addChangeListener(e -> changes.incrementAndGet());
        int width = control.getSegment(1).getPreferredSize().width;
        control.getSegment(1).doClick();
        assertEquals(1, control.getSelectedIndex());
        assertTrue(control.getSegment(1).isSelected());
        assertFalse(control.getSegment(0).isSelected());
        assertEquals(1, changes.get());
        assertEquals(width, control.getSegment(1).getPreferredSize().width, "selecting moved the options");
        assertEquals(UIManager.getFont("SegmentedControl.selectedFont"), control.getSegment(1).getFont());
        control.setSelectedIndex(1);
        assertEquals(1, changes.get());
        assertEquals(Arrays.asList("All 12", "Placed 4", "Shipped 6"), control.getOptions());
        assertEquals(Arrays.asList(), problems);
    }

    @Test
    void aScrimCoversTheWindowTakesTheMouseAndGoesAgain() {
        FlatLagoonModernLaf.setup();
        JRootPane window = new JRootPane();
        window.setSize(400, 300);
        window.doLayout();
        JLabel content = new JLabel("Cancel order?");
        Scrim scrim = Scrim.show(window, content);
        JLayeredPane layers = window.getLayeredPane();
        assertSame(layers, scrim.getParent());
        assertEquals(JLayeredPane.MODAL_LAYER, (Integer) layers.getLayer(scrim));
        assertEquals(layers.getBounds(), scrim.getBounds());
        assertTrue(scrim.isOpen());
        assertTrue(scrim.getMouseListeners().length > 0 && scrim.getMouseWheelListeners().length > 0);
        assertSame(scrim, content.getParent());

        Color veil = TOKENS.color("scrim");
        assertEquals(veil.getAlpha(), scrim.getBackground().getAlpha());
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 10, 10);
        scrim.setSize(10, 10);
        scrim.paint(g);
        g.dispose();
        float a = veil.getAlpha() / 255f;
        int expectedRed = Math.round(veil.getRed() * a + 255 * (1 - a));
        assertEquals(expectedRed, (image.getRGB(5, 5) >> 16) & 0xFF, 1.0);

        scrim.close();
        assertFalse(scrim.isOpen());
        assertNull(scrim.getParent());
        assertEquals(0, layers.getComponentCountInLayer(JLayeredPane.MODAL_LAYER));
    }

    @Test
    void theGalleryPaintsOffScreenWithoutAProblem() {
        FlatLagoonModernLaf.setup();
        BufferedImage image = LagoonModernGallery.render(LagoonModernGallery.gallery());
        assertEquals(TOKENS.color("ground").getRGB(), image.getRGB(1, 1));
        assertEquals(Arrays.asList(), problems);
    }

    /** The classic Lagoon is untouched: switching between the two leaves each its own. */
    @Test
    void switchingBetweenClassicAndModernKeepsEachItsOwn() {
        FlatLagoonModernLaf.setup();
        FlatLagoonLaf.setup();
        assertEquals(new Color(0x5F6265), UIManager.getColor("Component.borderColor"));
        assertNull(UIManager.get("Lagoon.color.ground"));
        assertNull(FlatLaf.getPreferredFontFamily());
        FlatLagoonModernLaf.setup();
        assertEquals(TOKENS.color("line"), UIManager.getColor("Component.borderColor"));
        assertEquals(TOKENS.fontFamily(), UIManager.getFont("defaultFont").getFamily());
    }
}
