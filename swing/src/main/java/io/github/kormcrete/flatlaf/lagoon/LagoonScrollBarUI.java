package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatScrollBarUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;

/**
 * Scroll bars with raised arrow buttons ({@link LagoonArrowButton}) and a framed thumb, set off from
 * the content they scroll by a line on the side facing it: left of a vertical bar (right, right to
 * left), above a horizontal one. {@code ScrollBar.width} includes that line.
 *
 * <p>Colours: {@code ScrollBar.lineColor} for the line, {@code ScrollBar.thumbBorderColor} for the
 * thumb's frame, {@code ScrollBar.gripHighlightColor} and {@code ScrollBar.gripShadowColor} for the
 * ridges of its grip. The frame and the ridges' shadow follow the thumb's state, as its fill does:
 * {@code ScrollBar.hoverThumbBorderColor} / {@code ScrollBar.pressedThumbBorderColor} and
 * {@code ScrollBar.hoverGripShadowColor} / {@code ScrollBar.pressedGripShadowColor}, each falling back
 * to the resting colour.
 *
 * <p>{@code Lagoon.shade} ({@link LagoonShade}) shades the thumb across its breadth and grooves the track;
 * {@code ScrollBar.thumbArc} rounds the thumb's ends, its frame included.
 */
public class LagoonScrollBarUI extends FlatScrollBarUI {

    /** The state FlatLaf last asked the thumb's colour for, so the frame and grip match the fill. */
    private boolean thumbHover;
    private boolean thumbPressed;
    /** {@code Lagoon.shade}, read when installed. */
    private String shade = LagoonShade.FLAT;
    /** Whether FlatLaf's fill, {@link #paintTrackOrThumb}, is filling the thumb (else the track). */
    private boolean fillingThumb;

    public static ComponentUI createUI(JComponent c) {
        return new LagoonScrollBarUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        shade = LagoonShade.current();
        Border border = scrollbar.getBorder();
        if (border == null || border instanceof UIResource) {
            scrollbar.setBorder(new Line());
        }
    }

    @Override
    protected void uninstallDefaults() {
        if (scrollbar.getBorder() instanceof Line) {
            scrollbar.setBorder(null);
        }
        super.uninstallDefaults();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return new Button(orientation);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new Button(orientation);
    }

    @Override
    protected Color getThumbColor(JComponent c, boolean hover, boolean pressed) {
        thumbHover = hover;
        thumbPressed = pressed;
        return super.getThumbColor(c, hover, pressed);
    }

    /** The key's pressed or hover variant for the thumb's state, else the resting colour. */
    private Color stateColor(String pressedKey, String hoverKey, String key) {
        Color color = thumbPressed ? UIManager.getColor(pressedKey) : null;
        if (color == null && (thumbHover || thumbPressed)) {
            color = UIManager.getColor(hoverKey);
        }
        return color != null ? color : UIManager.getColor(key);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        fillingThumb = false;
        super.paintTrack(g, c, trackBounds);
    }

    /** FlatLaf's fill of the track or thumb, with the shade's gradient in place of the plain colour it set. */
    @Override
    protected void paintTrackOrThumb(Graphics g, JComponent c, Rectangle bounds, Insets insets, int arc) {
        boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
        Paint paint = fillingThumb
                ? LagoonShade.thumb(shade, g.getColor(), bounds, vertical)
                : LagoonShade.groove(shade, g.getColor(), bounds, vertical,
                        vertical && !scrollbar.getComponentOrientation().isLeftToRight());
        if (paint != null) {
            ((Graphics2D) g).setPaint(paint);
        }
        super.paintTrackOrThumb(g, c, bounds, insets, arc);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        thumbHover = thumbPressed = false;
        fillingThumb = true;
        super.paintThumb(g, c, thumbBounds);
        fillingThumb = false;
        Color frame = stateColor("ScrollBar.pressedThumbBorderColor", "ScrollBar.hoverThumbBorderColor",
                "ScrollBar.thumbBorderColor");
        if (frame == null || thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        // Thumb insets are given for a vertical bar, as FlatLaf reads them.
        Insets insets = thumbInsets;
        if (scrollbar.getOrientation() == JScrollBar.HORIZONTAL) {
            insets = new Insets(insets.right, insets.top, insets.left, insets.bottom);
        }
        Rectangle r = FlatUIUtils.subtractInsets(thumbBounds, UIScale.scale(insets));
        float arc = Math.min(UIScale.scale((float) thumbArc), Math.min(r.width, r.height));
        g.setColor(frame);
        FlatUIUtils.paintOutline((Graphics2D) g, r.x, r.y, r.width, r.height, UIScale.scale(1f), arc);
        paintGrip(g, r);
    }

    /**
     * Three raised ridges across the middle of the thumb, each a light line over a dark one, when the
     * thumb is long enough to hold them with room to spare.
     */
    private void paintGrip(Graphics g, Rectangle thumb) {
        Color light = UIManager.getColor("ScrollBar.gripHighlightColor");
        Color dark = stateColor("ScrollBar.pressedGripShadowColor", "ScrollBar.hoverGripShadowColor",
                "ScrollBar.gripShadowColor");
        if (light == null || dark == null) {
            return;
        }
        boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
        int length = vertical ? thumb.height : thumb.width;
        int breadth = vertical ? thumb.width : thumb.height;
        int line = UIScale.scale(1);
        int pitch = 3 * line;
        int span = 3 * pitch - line;
        int margin = UIScale.scale(4);
        int across = breadth - 2 * margin;
        if (length < span + 2 * margin || across <= 0) {
            return;
        }
        int along = (length - span) / 2;
        for (int i = 0; i < 3; i++) {
            g.setColor(light);
            fillAcross(g, thumb, vertical, along + i * pitch, margin, across, line);
            g.setColor(dark);
            fillAcross(g, thumb, vertical, along + i * pitch + line, margin, across, line);
        }
    }

    private static void fillAcross(Graphics g, Rectangle thumb, boolean vertical, int along, int from, int across,
                                   int line) {
        if (vertical) {
            g.fillRect(thumb.x + from, thumb.y + along, across, line);
        } else {
            g.fillRect(thumb.x + along, thumb.y + from, line, across);
        }
    }

    /** The arrow buttons, square to the bar's breadth inside the line. */
    private class Button extends LagoonArrowButton {

        Button(int direction) {
            super(direction, 7, 4);
        }

        @Override
        public Dimension getPreferredSize() {
            if (!isShowButtons()) {
                return new Dimension();
            }
            int breadth = UIScale.scale(scrollBarWidth) - UIScale.scale(1);
            return new Dimension(breadth, breadth);
        }
    }

    /** The line on the side facing the content. */
    private static final class Line extends AbstractBorder implements UIResource {

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = insets.left = insets.bottom = insets.right = 0;
            int t = UIScale.scale(1);
            if (!vertical(c)) {
                insets.top = t;
            } else if (c.getComponentOrientation().isLeftToRight()) {
                insets.left = t;
            } else {
                insets.right = t;
            }
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color line = UIManager.getColor("ScrollBar.lineColor");
            if (line == null) {
                return;
            }
            int t = UIScale.scale(1);
            g.setColor(line);
            if (!vertical(c)) {
                g.fillRect(x, y, width, t);
            } else if (c.getComponentOrientation().isLeftToRight()) {
                g.fillRect(x, y, t, height);
            } else {
                g.fillRect(x + width - t, y, t, height);
            }
        }

        private static boolean vertical(Component c) {
            return !(c instanceof JScrollBar) || ((JScrollBar) c).getOrientation() == SwingConstants.VERTICAL;
        }
    }
}
