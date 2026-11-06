package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.util.UIScale;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Check boxes for dense forms: a 12px icon whose frame fills it, where FlatLaf's is 15px with a 14px box. The
 * checkmark and the indeterminate bar keep FlatLaf's shapes, shrunk to the smaller box.
 */
public class LagoonCheckBoxIcon extends FlatCheckBoxIcon {

    static final int SIZE = 12;

    /** FlatLaf draws its box 14px wide from x 1; its shapes are mapped onto this box at 0. */
    private static final float SHRINK = SIZE / 14f;

    @Override
    public int getIconWidth() {
        return size(getScale());
    }

    @Override
    public int getIconHeight() {
        return size(getScale());
    }

    static int size(float scale) {
        return Math.round(UIScale.scale(SIZE) * scale);
    }

    @Override
    protected void paintFocusBorder(Component c, Graphics2D g) {
        float wh = SIZE + focusWidth * 2;
        float arcwh = arc + focusWidth * 2;
        g.fill(new RoundRectangle2D.Float(-focusWidth, -focusWidth, wh, wh, arcwh, arcwh));
    }

    @Override
    protected void paintBorder(Component c, Graphics2D g, float borderWidth) {
        if (borderWidth == 0) {
            return;
        }
        g.fillRoundRect(0, 0, SIZE, SIZE, arc, arc);
    }

    @Override
    protected void paintBackground(Component c, Graphics2D g, float borderWidth) {
        float wh = SIZE - borderWidth * 2;
        float arcwh = arc - borderWidth;
        g.fill(new RoundRectangle2D.Float(borderWidth, borderWidth, wh, wh, arcwh, arcwh));
    }

    @Override
    protected void paintCheckmark(Component c, Graphics2D g) {
        Graphics2D shrunk = shrink(g);
        try {
            super.paintCheckmark(c, shrunk);
        } finally {
            shrunk.dispose();
        }
    }

    @Override
    protected void paintIndeterminate(Component c, Graphics2D g) {
        Graphics2D shrunk = shrink(g);
        try {
            super.paintIndeterminate(c, shrunk);
        } finally {
            shrunk.dispose();
        }
    }

    private static Graphics2D shrink(Graphics2D g) {
        Graphics2D shrunk = (Graphics2D) g.create();
        shrunk.scale(SHRINK, SHRINK);
        shrunk.translate(-1, 0);
        return shrunk;
    }
}
