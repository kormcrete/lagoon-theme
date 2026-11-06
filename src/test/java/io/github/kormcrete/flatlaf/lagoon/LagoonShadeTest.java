package io.github.kormcrete.flatlaf.lagoon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shades of scroll bars and tabs: Gloss (the default), Soft ({@link FlatLagoonSoftLaf}) and flat
 * ({@code Lagoon.shade = flat}). Headless; every pixel is compared with another pixel of the same picture.
 */
class LagoonShadeTest {

    @AfterEach
    void resetLookAndFeel() throws Exception {
        UIManager.put("Lagoon.shade", null);
        UIManager.put("TabbedPane.cardTabArc", null);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
    }

    @Test
    void glossIsTheDefaultShadeWithRoundedThumbsAndTabs() {
        FlatLagoonLaf.setup();
        assertEquals("gloss", UIManager.getString("Lagoon.shade"));
        assertEquals(6, UIManager.getInt("ScrollBar.thumbArc"));
        assertEquals(6, UIManager.getInt("TabbedPane.cardTabArc"));
    }

    @Test
    void softIsItsOwnThemeOnLagoon() {
        assertTrue(FlatLagoonSoftLaf.setup());
        assertTrue(UIManager.getLookAndFeel() instanceof FlatLagoonLaf);
        assertEquals("Lagoon Soft", UIManager.getLookAndFeel().getName());
        assertEquals("soft", UIManager.getString("Lagoon.shade"));
        assertEquals(4, UIManager.getInt("ScrollBar.thumbArc"));
        assertEquals(4, UIManager.getInt("TabbedPane.cardTabArc"));
        // Everything else is Lagoon's.
        assertEquals(new Color(0x5F6265), UIManager.getColor("Component.borderColor"));
    }

    /** A gloss thumb is a tube: light on its leading side, darkest along the middle, glowing again further on. */
    @Test
    void aGlossThumbIsLightThenDarkAlongTheMiddleThenGlowing() {
        FlatLagoonLaf.setup();
        float[] row = thumbRow(bar(), false);
        assertTrue(row[3] > row[8] + 8, "leading side lighter than the middle " + str(row));
        assertTrue(row[12] > row[8] + 8, "glow lighter than the middle " + str(row));
    }

    @Test
    void aSoftThumbDarkensSteadilyAcrossItsBreadth() {
        FlatLagoonSoftLaf.setup();
        float[] row = thumbRow(bar(), false);
        assertTrue(row[3] > row[8] && row[8] > row[13], "lighter to darker " + str(row));
    }

    @Test
    void aFlatThumbIsOneColour() {
        FlatLagoonLaf.setup();
        UIManager.put("Lagoon.shade", "flat");
        float[] row = thumbRow(bar(), false);
        assertEquals(row[3], row[8]);
        assertEquals(row[8], row[13]);
    }

    /** The mouse on the bar deepens the whole tube, its shading kept. */
    @Test
    void aGlossThumbDeepensWhileTheMouseIsOnTheBar() {
        FlatLagoonLaf.setup();
        JScrollBar bar = bar();
        float[] resting = thumbRow(bar, false);
        float[] hovered = thumbRow(bar, true);
        assertTrue(average(hovered) < average(resting) - 8, str(resting) + " / " + str(hovered));
        assertTrue(hovered[3] > hovered[8] && hovered[12] > hovered[8], "still a tube " + str(hovered));
    }

    @Test
    void theTrackIsAGrooveShadedFromItsOuterEdge() {
        FlatLagoonLaf.setup();
        BufferedImage image = paint(bar());
        // Below the thumb, above the lower button; x 0 is the line towards the content.
        assertTrue(luminance(image.getRGB(1, 75)) < luminance(image.getRGB(13, 75)) - 3);
    }

    @Test
    void aFlatTrackIsOneColour() {
        FlatLagoonLaf.setup();
        UIManager.put("Lagoon.shade", "flat");
        BufferedImage image = paint(bar());
        assertEquals(image.getRGB(1, 75), image.getRGB(13, 75));
    }

    @Test
    void aRoundedThumbLeavesItsCornersToTheTrack() {
        FlatLagoonLaf.setup();
        int frame = UIManager.getColor("ScrollBar.thumbBorderColor").getRGB();
        BufferedImage image = paint(bar());
        int top = thumbTop(image, frame);
        assertEquals(frame, image.getRGB(8, top), "the frame across the top");
        assertNotEquals(frame, image.getRGB(1, top), "but not in the corner");
    }

    @Test
    void anUnselectedTabsOuterCornersAreRounded() {
        FlatLagoonLaf.setup();
        int frame = UIManager.getColor("TabbedPane.unselectedBorderColor").getRGB();
        JTabbedPane tabs = tabs();
        BufferedImage image = paint(tabs);
        Rectangle tab = tabs.getBoundsAt(1);
        int top = tab.y + UIManager.getInt("TabbedPane.selectedTabRaise");
        assertEquals(frame, image.getRGB(tab.x + tab.width / 2, top), "the frame across the top");
        assertNotEquals(frame, image.getRGB(tab.x + tab.width - 1, top), "but not in the corner");
    }

    @Test
    void aFlatTabKeepsSquareCorners() {
        FlatLagoonLaf.setup();
        UIManager.put("Lagoon.shade", "flat");
        UIManager.put("TabbedPane.cardTabArc", 0);
        int frame = UIManager.getColor("TabbedPane.unselectedBorderColor").getRGB();
        JTabbedPane tabs = tabs();
        BufferedImage image = paint(tabs);
        Rectangle tab = tabs.getBoundsAt(1);
        assertEquals(frame, image.getRGB(tab.x + tab.width - 1, tab.y + UIManager.getInt("TabbedPane.selectedTabRaise")));
    }

    /** Hovered, an unselected tab's fill stays inside its rounded corners. */
    @Test
    void aHoveredTabKeepsItsRoundedCorners() {
        FlatLagoonLaf.setup();
        JTabbedPane tabs = tabs();
        Rectangle tab = tabs.getBoundsAt(1);
        tabs.dispatchEvent(new MouseEvent(tabs, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                tab.x + tab.width / 2, tab.y + tab.height / 2, 0, false));
        BufferedImage image = paint(tabs);
        int top = tab.y + UIManager.getInt("TabbedPane.selectedTabRaise");
        int hover = UIManager.getColor("TabbedPane.hoverColor").getRGB();
        assertEquals(hover, image.getRGB(tab.x + 3, top + tab.height / 2), "hovered");
        assertNotEquals(hover, image.getRGB(tab.x + tab.width - 1, top), "the corner is not filled");
    }

    /** A gloss tab is a pillow: light under its top, darker past the middle, lighter again towards the content. */
    @Test
    void aGlossTabIsLightThenDarkerThenLighterAgain() {
        FlatLagoonLaf.setup();
        float[] column = tabColumn();
        int n = column.length;
        assertTrue(column[1] > column[n * 55 / 100] + 4, str(column));
        assertTrue(column[n - 2] > column[n * 55 / 100] + 2, str(column));
    }

    @Test
    void aSoftTabDarkensTowardsTheContent() {
        FlatLagoonSoftLaf.setup();
        float[] column = tabColumn();
        int n = column.length;
        assertTrue(column[1] > column[n / 2] && column[n / 2] > column[n - 2], str(column));
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static JScrollBar bar() {
        JScrollBar bar = new JScrollBar(JScrollBar.VERTICAL, 0, 40, 0, 100);
        bar.setSize(16, 100);
        bar.doLayout();
        return bar;
    }

    private static JTabbedPane tabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Orders", new JPanel());
        tabs.addTab("Customers", new JPanel());
        tabs.setSize(300, 200);
        tabs.doLayout();
        return tabs;
    }

    private static BufferedImage paint(java.awt.Component c) {
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
        return image;
    }

    /** The first row of the thumb's frame down the bar's centre column. */
    private static int thumbTop(BufferedImage image, int frame) {
        for (int y = 0; y < image.getHeight(); y++) {
            if (image.getRGB(8, y) == frame) {
                return y;
            }
        }
        throw new AssertionError("no thumb frame");
    }

    /** Luminance across the thumb, x 0..15, a few rows below its top (clear of the round end and the grip). */
    private static float[] thumbRow(JScrollBar bar, boolean hover) {
        if (hover) {
            // On the track below the thumb, not on the thumb itself.
            bar.dispatchEvent(new MouseEvent(bar, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 8, 80, 0, false));
        }
        int frame = UIManager.getColor(hover ? "ScrollBar.hoverThumbBorderColor" : "ScrollBar.thumbBorderColor").getRGB();
        BufferedImage image = paint(bar);
        int y = thumbTop(image, frame) + 5;
        float[] row = new float[16];
        for (int x = 0; x < row.length; x++) {
            row[x] = luminance(image.getRGB(x, y));
        }
        return row;
    }

    /** Luminance down an unselected tab, just inside its leading edge, from below its frame to the content's line. */
    private static float[] tabColumn() {
        JTabbedPane tabs = tabs();
        BufferedImage image = paint(tabs);
        Rectangle tab = tabs.getBoundsAt(1);
        int top = tab.y + UIManager.getInt("TabbedPane.selectedTabRaise") + 1;
        int bottom = tab.y + tab.height;
        float[] column = new float[bottom - top];
        for (int i = 0; i < column.length; i++) {
            column[i] = luminance(image.getRGB(tab.x + 4, top + i));
        }
        return column;
    }

    private static float luminance(int rgb) {
        Color c = new Color(rgb, true);
        return .299f * c.getRed() + .587f * c.getGreen() + .114f * c.getBlue();
    }

    private static float average(float[] values) {
        float sum = 0;
        for (int i = 2; i < values.length - 2; i++) {
            sum += values[i];
        }
        return sum / (values.length - 4);
    }

    private static String str(float[] values) {
        StringBuilder s = new StringBuilder();
        for (float v : values) {
            s.append(Math.round(v)).append(' ');
        }
        return s.toString().trim();
    }
}
