package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.ComponentUI;
import java.awt.Dimension;

/**
 * Labels sized by {@link CompoundSize}: with {@code Lagoon.labelGap}, the icon-text gap is kept after the
 * text, icon or not, table cells and tree rows included. List renderers keep FlatLaf's size.
 */
public class LagoonLabelUI extends FlatLabelUI {

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c)
                ? FlatUIUtils.createSharedUI(LagoonLabelUI.class, () -> new LagoonLabelUI(true))
                : new LagoonLabelUI(false);
    }

    protected LagoonLabelUI(boolean shared) {
        super(shared);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return adjust(c, super.getPreferredSize(c));
    }

    // No getMinimumSize: Swing's derives it from getPreferredSize, gap included.

    private static Dimension adjust(JComponent c, Dimension size) {
        // A list renderer draws a list item or combo value: its size is the item's, not a label's. A table cell
        // keeps the gap, so a column fitted to its widest cell has a label's room; a tree row too, so a row of
        // text alone is as wide as the same text in a label.
        if (c instanceof ListCellRenderer) {
            return size;
        }
        JLabel l = (JLabel) c;
        return CompoundSize.adjust(size, l.getText(), l.getIcon(), l.getIconTextGap(),
                l.getHorizontalTextPosition(), l.getVerticalTextPosition());
    }
}
