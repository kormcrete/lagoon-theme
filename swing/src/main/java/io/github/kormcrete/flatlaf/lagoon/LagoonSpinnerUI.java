package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatSpinnerUI;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * Spinners with FlatLaf's arrows, or, with {@code Lagoon.raisedArrowButtons}, two small raised buttons
 * ({@link LagoonArrowButton}), stacked and set inside the frame with a one-pixel gap, each
 * {@value #BUTTON_WIDTH}px wide. Pair the switch with {@code Spinner.buttonStyle = none}, so FlatLaf paints
 * no button background of its own.
 */
public class LagoonSpinnerUI extends FlatSpinnerUI {

    static final int BUTTON_WIDTH = 16;

    public static ComponentUI createUI(JComponent c) {
        return new LagoonSpinnerUI();
    }

    private static boolean raised() {
        return UIManager.getBoolean("Lagoon.raisedArrowButtons");
    }

    @Override
    protected Component createNextButton() {
        if (!raised()) {
            return super.createNextButton();
        }
        Component b = new LagoonArrowButton(SwingConstants.NORTH, 6, 3);
        b.setName("Spinner.nextButton");
        installNextButtonListeners(b);
        return b;
    }

    @Override
    protected Component createPreviousButton() {
        if (!raised()) {
            return super.createPreviousButton();
        }
        Component b = new LagoonArrowButton(SwingConstants.SOUTH, 6, 3);
        b.setName("Spinner.previousButton");
        installPreviousButtonListeners(b);
        return b;
    }

    @Override
    protected LayoutManager createLayout() {
        return new DelegatingLayout(super.createLayout()) {
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                Dimension d = super.preferredLayoutSize(parent);
                if (!hasRaisedButtons(parent)) {
                    return d;
                }
                Insets insets = parent.getInsets();
                // FlatLaf adds a square button area as wide as the inner height; these buttons are narrower
                int square = d.height - insets.top - insets.bottom;
                d.width += buttonsWidth() - square;
                return d;
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return hasRaisedButtons(parent) ? preferredLayoutSize(parent) : super.minimumLayoutSize(parent);
            }

            @Override
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);
                Component next = null, previous = null, editor = null;
                for (Component c : parent.getComponents()) {
                    if ("Spinner.nextButton".equals(c.getName())) next = c;
                    else if ("Spinner.previousButton".equals(c.getName())) previous = c;
                    else editor = c;
                }
                if (!(next instanceof LagoonArrowButton) || !(previous instanceof LagoonArrowButton)) {
                    return;
                }
                Insets insets = parent.getInsets();
                int gap = scale(1);
                int w = scale(BUTTON_WIDTH);
                int h = parent.getHeight() - insets.top - insets.bottom - 2 * gap;
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                int x = ltr ? parent.getWidth() - insets.right - gap - w : insets.left + gap;
                int y = insets.top + gap;
                int upper = (h + 1) / 2;
                next.setBounds(x, y, w, upper);
                previous.setBounds(x, y + upper, w, h - upper);
                if (editor != null) {
                    int left = ltr ? insets.left : x + w + gap;
                    int right = ltr ? x - gap : parent.getWidth() - insets.right;
                    editor.setBounds(left, insets.top, right - left, parent.getHeight() - insets.top - insets.bottom);
                }
            }
        };
    }

    private static boolean hasRaisedButtons(Container spinner) {
        for (Component c : spinner.getComponents()) {
            if (c instanceof LagoonArrowButton) {
                return true;
            }
        }
        return false;
    }

    private static int buttonsWidth() {
        return scale(BUTTON_WIDTH) + 2 * scale(1);
    }
}
