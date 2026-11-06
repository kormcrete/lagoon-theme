package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Objects;

/**
 * A status pill with a dot: "● Placed". A label in the style class of its {@link LagoonTone}
 * ({@code [style]Label.pill-success} and the like: a rounded tint, the tone's ink, the caption font), with a dot
 * of {@code StatusPill.dotWidth} in the ink.
 */
public class StatusPill extends JLabel {

    private LagoonTone tone;

    public StatusPill(String text, LagoonTone tone) {
        super(text);
        setIcon(new Dot());
        setTone(tone);
    }

    public LagoonTone getTone() {
        return tone;
    }

    /** Changes the tone; fires the bound property {@code "tone"}. */
    public void setTone(LagoonTone tone) {
        LagoonTone old = this.tone;
        this.tone = Objects.requireNonNull(tone, "tone");
        putClientProperty(FlatClientProperties.STYLE_CLASS, tone.pillStyleClass());
        firePropertyChange("tone", old, tone);
    }

    /** A filled circle in the pill's ink. */
    private static final class Dot implements Icon {
        private static int size() {
            int width = UIManager.getInt("StatusPill.dotWidth");
            return UIScale.scale(width > 0 ? width : 6);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                FlatUIUtils.setRenderingHints(g2);
                g2.setColor(c.getForeground());
                int size = size();
                g2.fill(new Ellipse2D.Float(x, y, size, size));
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size();
        }

        @Override
        public int getIconHeight() {
            return size();
        }
    }
}
