package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The arrow buttons of combo boxes, spinners, scroll bars and tab areas have no border. Their constructor
 * clears it, but {@code updateUI()} (each look-and-feel switch, through
 * {@link SwingUtilities#updateComponentTreeUI}) would install {@code Button.border} on them again: the
 * button delegates keep them border-less.
 */
class ArrowButtonUpdateUITest {

    /** An application's custom defaults that turn every switch on, raised arrow buttons included. */
    private static final String DENSE = "io.github.kormcrete.flatlaf.lagoon.dense";

    @AfterEach
    void resetLookAndFeel() throws Exception {
        FlatLaf.unregisterCustomDefaultsSource(DENSE);
        UIManager.put("defaultFont", null);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
    }

    @Test
    void lagoonKeepsArrowButtonsBorderlessAcrossUpdateUI() {
        FlatLagoonLaf.setup();
        assertUnchangedByUpdateUI();
    }

    @Test
    void raisedArrowButtonsStayBorderlessAcrossUpdateUI() {
        FlatLaf.registerCustomDefaultsSource(DENSE);
        FlatLagoonLaf.setup();
        assertUnchangedByUpdateUI();
    }

    @Test
    void lagoonSoftKeepsArrowButtonsBorderlessAcrossUpdateUI() {
        FlatLagoonSoftLaf.setup();
        assertUnchangedByUpdateUI();
    }

    @Test
    void lagoonDarkKeepsArrowButtonsBorderlessAcrossUpdateUI() {
        FlatLagoonDarkLaf.setup();
        assertUnchangedByUpdateUI();
    }

    @Test
    void lagoonModernKeepsArrowButtonsBorderlessAcrossUpdateUI() {
        FlatLagoonModernLaf.setup();
        assertUnchangedByUpdateUI();
    }

    @Test
    void switchingFromGlossToSoftGivesTheArrowButtonsOfAFreshSoftStart() {
        FlatLagoonLaf.setup();
        JPanel switched = components();
        FlatLagoonSoftLaf.setup();
        SwingUtilities.updateComponentTreeUI(switched);
        assertEquals(describe(components()), describe(switched));
    }

    @Test
    void switchingFromSoftToGlossGivesTheArrowButtonsOfAFreshGlossStart() {
        FlatLagoonSoftLaf.setup();
        JPanel switched = components();
        FlatLagoonLaf.setup();
        SwingUtilities.updateComponentTreeUI(switched);
        assertEquals(describe(components()), describe(switched));
    }

    private static void assertUnchangedByUpdateUI() {
        JPanel panel = components();
        List<BasicArrowButton> buttons = arrowButtons(panel);
        assertFalse(buttons.isEmpty());
        for (BasicArrowButton b : buttons) {
            assertNull(b.getBorder(), b.getClass().getName());
        }
        List<String> before = describe(panel);
        SwingUtilities.updateComponentTreeUI(panel);
        SwingUtilities.updateComponentTreeUI(panel);
        assertEquals(before, describe(panel));
    }

    /** A combo box, a spinner, both scroll bars and a scrolling tabbed pane, laid out. */
    private static JPanel components() {
        JPanel panel = new JPanel();
        panel.add(new JComboBox<>(new String[] {"one", "two"}));
        JComboBox<String> editable = new JComboBox<>(new String[] {"one", "two"});
        editable.setEditable(true);
        panel.add(editable);
        panel.add(new JSpinner());
        panel.add(new JScrollBar(JScrollBar.VERTICAL));
        panel.add(new JScrollBar(JScrollBar.HORIZONTAL));
        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        for (int i = 0; i < 12; i++) {
            tabs.addTab("Tab number " + i, new JLabel("content " + i));
        }
        tabs.setSize(200, 100);
        panel.add(tabs);
        layOut(panel);
        return panel;
    }

    private static void layOut(Component c) {
        c.doLayout();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                layOut(child);
            }
        }
    }

    private static List<BasicArrowButton> arrowButtons(Component c) {
        List<BasicArrowButton> found = new ArrayList<>();
        collect(c, found);
        return found;
    }

    private static void collect(Component c, List<BasicArrowButton> found) {
        if (c instanceof BasicArrowButton) {
            found.add((BasicArrowButton) c);
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                collect(child, found);
            }
        }
    }

    /** Each arrow button's class, border, insets and preferred size, in tree order. */
    private static List<String> describe(JPanel panel) {
        List<String> lines = new ArrayList<>();
        for (BasicArrowButton b : arrowButtons(panel)) {
            JComponent parent = (JComponent) b.getParent();
            lines.add(parent.getClass().getSimpleName() + " > " + b.getClass().getName()
                    + " border=" + (b.getBorder() == null ? "null" : b.getBorder().getClass().getName())
                    + " insets=" + b.getInsets() + " preferred=" + b.getPreferredSize());
        }
        return lines;
    }
}
