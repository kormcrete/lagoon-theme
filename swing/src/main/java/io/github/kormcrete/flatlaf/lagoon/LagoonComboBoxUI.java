package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatComboBoxUI;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Container;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * Combo boxes with FlatLaf's arrow, or, with {@code Lagoon.raisedArrowButtons}, a small raised button
 * ({@link LagoonArrowButton}) set inside the frame with a one-pixel gap, instead of a band separated by a
 * line. The box keeps FlatLaf's size: the button and its gaps take the width FlatLaf gives its square
 * button. Pair the switch with {@code ComboBox.buttonStyle = none}, so FlatLaf paints no button background
 * of its own. {@code ComboBox.editorMargins}, when set, pads an editable box's text field.
 */
public class LagoonComboBoxUI extends FlatComboBoxUI {

    public static ComponentUI createUI(JComponent c) {
        return new LagoonComboBoxUI();
    }

    @Override
    protected JButton createArrowButton() {
        return UIManager.getBoolean("Lagoon.raisedArrowButtons")
                ? new LagoonArrowButton(SwingConstants.SOUTH, 7, 4) : super.createArrowButton();
    }

    private boolean raised() {
        return arrowButton instanceof LagoonArrowButton && arrowButton.isVisible();
    }

    /**
     * FlatLaf pads an editable box's text field like a list value; {@code ComboBox.editorMargins} pads it like a
     * field instead. Only for FlatLaf's borderless editor: a text field with a border of its own keeps FlatLaf's.
     */
    @Override
    protected void configureEditor() {
        super.configureEditor();
        Insets margins = UIManager.getInsets("ComboBox.editorMargins");
        if (margins != null && editor instanceof JTextField) {
            Insets own = ((JTextField) editor).getInsets();
            if (own.top == 0 && own.left == 0 && own.bottom == 0 && own.right == 0) {
                ((JTextField) editor).putClientProperty(FlatClientProperties.TEXT_FIELD_PADDING, margins);
            }
        }
    }

    @Override
    protected LayoutManager createLayoutManager() {
        LayoutManager flat = super.createLayoutManager();
        return new DelegatingLayout(flat) {
            @Override
            public void layoutContainer(Container parent) {
                super.layoutContainer(parent);
                if (!raised()) {
                    return;
                }
                Insets insets = comboBox.getInsets();
                int gap = scale(1);
                int slot = arrowButton.getWidth();
                int size = slot - 2 * gap;
                int x = arrowButton.getX() + gap;
                int y = insets.top + gap;
                int h = comboBox.getHeight() - insets.top - insets.bottom - 2 * gap;
                arrowButton.setBounds(x, y, size, h);
                if (editor != null && comboBox.isEditable()) {
                    editor.setBounds(rectangleForCurrentValue());
                }
            }
        };
    }

    @Override
    protected Rectangle rectangleForCurrentValue() {
        Rectangle r = super.rectangleForCurrentValue();
        if (raised() && comboBox.getComponentOrientation().isLeftToRight()) {
            // up to the gap before the button, not to where FlatLaf's button band began
            int end = arrowButton.getX() - scale(1);
            if (end > r.x) {
                r.width = Math.min(r.width, end - r.x);
            }
        }
        return r;
    }
}
