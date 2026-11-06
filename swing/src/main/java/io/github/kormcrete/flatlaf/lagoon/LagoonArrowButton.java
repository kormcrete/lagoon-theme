package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatArrowButton;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * The small raised button of a combo box, spinner or scroll bar: its own rounded frame, the
 * {@link LagoonSheen} face and a solid triangle; the frame turns blue on hover. Disabled, it is flat with
 * a grey frame and arrow. The combo box and spinner delegates place it inside their frame with a
 * one-pixel gap; the scroll bar delegate at the ends of its track.
 */
class LagoonArrowButton extends FlatArrowButton {

    private final int arrowWidth;
    private final int arrowHeight;

    LagoonArrowButton(int direction, int arrowWidth, int arrowHeight) {
        super(direction, "triangle", UIManager.getColor("ComboBox.buttonArrowColor"),
                UIManager.getColor("ComboBox.buttonDisabledArrowColor"), null, null, null, null);
        this.arrowWidth = arrowWidth;
        this.arrowHeight = arrowHeight;
        setFocusable(false);
        setRequestFocusEnabled(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            int w = getWidth();
            int h = getHeight();
            float t = scale(1f);
            float arc = scale(3f);
            RoundRectangle2D.Float outer = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
            boolean enabled = isEnabled();
            if (enabled) {
                LagoonSheen.paint(g2, outer, t, t, w - 2 * t, h - 2 * t, isPressed());
            } else {
                g2.setColor(UIManager.getColor("Button.disabledBackground"));
                g2.fill(outer);
            }
            g2.setColor(UIManager.getColor(!enabled ? "Component.disabledBorderColor"
                    : isHover() ? "Button.hoverBorderColor" : "Component.borderColor"));
            FlatUIUtils.paintOutline(g2, 0, 0, w, h, t, arc);

            // arrowWidth runs across the arrow's direction, arrowHeight along it.
            boolean across = getDirection() == SwingConstants.EAST || getDirection() == SwingConstants.WEST;
            int aw = scale(across ? arrowHeight : arrowWidth);
            int ah = scale(across ? arrowWidth : arrowHeight);
            int ax = (w - aw) / 2;
            int ay = (h - ah) / 2;
            g2.setColor(enabled ? UIManager.getColor("ComboBox.buttonArrowColor")
                    : UIManager.getColor("ComboBox.buttonDisabledArrowColor"));
            fillTriangle(g2, ax, ay, aw, ah, getDirection());
        } finally {
            g2.dispose();
        }
    }

    /** Line by line from the base, without antialiasing: 7, 5, 3, 1 pixels for a 7x4 arrow. */
    private static void fillTriangle(Graphics2D g, int x, int y, int w, int h, int direction) {
        boolean across = direction == SwingConstants.EAST || direction == SwingConstants.WEST;
        int base = across ? h : w;
        int length = across ? w : h;
        for (int i = 0; i < length; i++) {
            int inset = Math.round(i * base / (2f * length));
            switch (direction) {
                case SwingConstants.NORTH: g.fillRect(x + inset, y + h - 1 - i, w - 2 * inset, 1); break;
                case SwingConstants.WEST:  g.fillRect(x + w - 1 - i, y + inset, 1, h - 2 * inset); break;
                case SwingConstants.EAST:  g.fillRect(x + i, y + inset, 1, h - 2 * inset); break;
                default:                   g.fillRect(x + inset, y + i, w - 2 * inset, 1); break;
            }
        }
    }
}
