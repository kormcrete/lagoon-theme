package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatTreeUI;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.tree.TreePath;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * Trees that honour the standard {@code JTree.lineStyle} client property: {@code "None"} paints no lines, as
 * it does in Swing's own look-and-feels; any other value, or none, keeps {@code Tree.paintLines}.
 */
public class LagoonTreeUI extends FlatTreeUI {

    public static ComponentUI createUI(JComponent c) {
        return new LagoonTreeUI();
    }

    private boolean noLines() {
        return "None".equals(tree.getClientProperty("JTree.lineStyle"));
    }

    @Override
    protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds,
            TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
        if (!noLines()) {
            super.paintHorizontalPartOfLeg(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
        }
    }

    @Override
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
        if (!noLines()) {
            super.paintVerticalPartOfLeg(g, clipBounds, insets, path);
        }
    }
}
