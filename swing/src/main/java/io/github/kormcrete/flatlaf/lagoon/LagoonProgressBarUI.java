package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatProgressBarUI;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * A progress bar whose track is framed like a field, with the bar set inside it and framed too, so an empty or
 * nearly empty bar still reads as one.
 *
 * <p>The frame is drawn on the outermost line of the bar's insets ({@code ProgressBar.border} must leave room for
 * it, plus the gap to the bar); the track fills everything inside the frame. Colours: {@code ProgressBar.borderColor}
 * for the track's frame, {@code ProgressBar.background} for the track, {@code ProgressBar.foreground} for the bar
 * and {@code ProgressBar.barBorderColor} for its frame; a missing frame colour leaves that frame out.
 */
public class LagoonProgressBarUI extends FlatProgressBarUI {

    private Color borderColor;
    private Color barBorderColor;

    public static ComponentUI createUI(JComponent c) {
        return new LagoonProgressBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        borderColor = UIManager.getColor("ProgressBar.borderColor");
        barBorderColor = UIManager.getColor("ProgressBar.barBorderColor");
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int w = progressBar.getWidth();
        int h = progressBar.getHeight();
        int line = UIScale.scale(1);
        boolean framed = borderColor != null && w > 2 * line && h > 2 * line;
        if (framed) {
            g.setColor(progressBar.getBackground());
            g.fillRect(line, line, w - 2 * line, h - 2 * line);
        }
        super.paint(g, c);
        if (framed) {
            outline(g, borderColor, new Rectangle(0, 0, w, h), line);
        }
        Rectangle bar = barBounds();
        if (barBorderColor != null && bar != null && bar.width >= 2 * line && bar.height >= 2 * line) {
            outline(g, barBorderColor, bar, line);
        }
    }

    /** Where FlatLaf painted the bar (or the indeterminate box), or null when nothing is filled. */
    private Rectangle barBounds() {
        if (progressBar.isIndeterminate()) {
            return boxRect != null ? new Rectangle(boxRect) : null;
        }
        Insets insets = progressBar.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = progressBar.getWidth() - insets.left - insets.right;
        int height = progressBar.getHeight() - insets.top - insets.bottom;
        if (width <= 0 || height <= 0) {
            return null;
        }
        int amount = getAmountFull(insets, width, height);
        if (amount <= 0) {
            return null;
        }
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            boolean ltr = progressBar.getComponentOrientation().isLeftToRight();
            return new Rectangle(ltr ? x : x + width - amount, y, amount, height);
        }
        return new Rectangle(x, y + height - amount, width, amount);
    }

    private static void outline(Graphics g, Color color, Rectangle r, int line) {
        g.setColor(color);
        g.fillRect(r.x, r.y, r.width, line);
        g.fillRect(r.x, r.y + r.height - line, r.width, line);
        g.fillRect(r.x, r.y, line, r.height);
        g.fillRect(r.x + r.width - line, r.y, line, r.height);
    }
}
