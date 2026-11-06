package io.github.kormcrete.flatlaf.lagoon;

import javax.swing.ButtonModel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * The raised face of buttons and arrow buttons: a soft gradient from a light top through a middle tone
 * to a darker foot, under a one-pixel highlight. Pressed, the gradient turns over and darkens; hover is
 * shown by the frame ({@code Button.hoverBorderColor}), not by the face.
 *
 * <p>Colours: {@code Lagoon.sheen.top}, {@code .middle}, {@code .bottom}, {@code .highlight}, and the
 * {@code Lagoon.sheen.pressed*} keys; the gradient scales with the face, so a 9px spinner button and a
 * 25px button share it.
 */
final class LagoonSheen {

    private static final float[] STOPS = {0f, .45f, 1f};

    private LagoonSheen() {
    }

    static boolean pressed(ButtonModel model, boolean selected) {
        return (model.isArmed() && model.isPressed()) || selected;
    }

    /**
     * Fills {@code shape} with the face. {@code x, y, w, h} is the area inside the frame line, where the
     * highlight goes; {@code shape} may be larger (the frame is painted over its edge).
     */
    static void paint(Graphics2D g, Shape shape, float x, float y, float w, float h, boolean pressed) {
        Color top = UIManager.getColor(pressed ? "Lagoon.sheen.pressedTop" : "Lagoon.sheen.top");
        Color middle = UIManager.getColor(pressed ? "Lagoon.sheen.pressedMiddle" : "Lagoon.sheen.middle");
        Color bottom = UIManager.getColor(pressed ? "Lagoon.sheen.pressedBottom" : "Lagoon.sheen.bottom");
        if (top == null || middle == null || bottom == null || h < 2) {
            g.setColor(middle != null ? middle : UIManager.getColor("Button.background"));
            g.fill(shape);
            return;
        }
        g.setPaint(new LinearGradientPaint(0, y, 0, y + h, STOPS, new Color[]{top, middle, bottom}));
        g.fill(shape);
        Color highlight = UIManager.getColor("Lagoon.sheen.highlight");
        if (!pressed && highlight != null) {
            g.setColor(highlight);
            g.fill(new Rectangle2D.Float(x, y, w, scale(1f)));
        }
    }
}
