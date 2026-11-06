package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.icons.FlatRadioButtonIcon;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Radio buttons the size of {@link LagoonCheckBoxIcon}: a 12px circle filling the icon, where FlatLaf's is 15px.
 * The centre dot keeps its share of the circle ({@code RadioButton.icon.centerDiameter} is given for 15px).
 */
public class LagoonRadioButtonIcon extends FlatRadioButtonIcon {

    private static final int SIZE = LagoonCheckBoxIcon.SIZE;

    @Override
    public int getIconWidth() {
        return LagoonCheckBoxIcon.size(getScale());
    }

    @Override
    public int getIconHeight() {
        return LagoonCheckBoxIcon.size(getScale());
    }

    @Override
    protected void paintFocusBorder(Component c, Graphics2D g) {
        float wh = SIZE + focusWidth * 2;
        g.fill(new Ellipse2D.Float(-focusWidth, -focusWidth, wh, wh));
    }

    @Override
    protected void paintBorder(Component c, Graphics2D g, float borderWidth) {
        if (borderWidth == 0) {
            return;
        }
        g.fillOval(0, 0, SIZE, SIZE);
    }

    @Override
    protected void paintBackground(Component c, Graphics2D g, float borderWidth) {
        float wh = SIZE - borderWidth * 2;
        g.fill(new Ellipse2D.Float(borderWidth, borderWidth, wh, wh));
    }

    @Override
    protected void paintCheckmark(Component c, Graphics2D g) {
        float diameter = centerDiameter * SIZE / 15f;
        float xy = (SIZE - diameter) / 2f;
        g.fill(new Ellipse2D.Float(xy, xy, diameter, diameter));
    }
}
