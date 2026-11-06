package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * Card tabs where the unselected tabs are framed and shaded too, so the selected one reads as
 * lifted onto the content. FlatLaf paints the selected tab as usual.
 *
 * <p>Colours: {@code TabbedPane.unselectedBorderColor} for the frame and
 * {@code TabbedPane.unselectedBottomColor}, where the shade ends (it starts at the tab background).
 * {@code TabbedPane.contentMargins} is the room between the content's frame and the content, given as
 * insets for tabs on top (its top is the side next to the tabs) and turned with the tabs; the frame
 * itself stays one line.
 *
 * <p>{@code TabbedPane.selectedTabRaise} (pixels; 0 for none) lowers the unselected tabs from the outer edge
 * by that much, so the selected tab stands above its neighbours; their title, icon or tab component stays
 * centred in what is left.
 *
 * <p>{@code Lagoon.shade} ({@link LagoonShade}) shades the unselected tabs; {@code TabbedPane.cardTabArc}
 * rounds the corners on their outer edge, as FlatLaf rounds the selected tab's.
 */
public class LagoonTabbedPaneUI extends FlatTabbedPaneUI {

    private Color unselectedBorderColor;
    private Color unselectedBottomColor;
    private Insets contentMargins;
    private int selectedTabRaise;
    /** {@code Lagoon.shade}, read when installed. */
    private String shade = LagoonShade.FLAT;

    public static ComponentUI createUI(JComponent c) {
        return new LagoonTabbedPaneUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        unselectedBorderColor = UIManager.getColor("TabbedPane.unselectedBorderColor");
        unselectedBottomColor = UIManager.getColor("TabbedPane.unselectedBottomColor");
        contentMargins = UIManager.getInsets("TabbedPane.contentMargins");
        selectedTabRaise = UIManager.getInt("TabbedPane.selectedTabRaise");
        shade = LagoonShade.current();
    }

    // FlatLaf paints the frame from the separator height, not from these insets, so the room added here stays blank.
    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        Insets insets = super.getContentBorderInsets(tabPlacement);
        if (contentMargins == null || (insets.top == 0 && insets.left == 0 && insets.bottom == 0 && insets.right == 0)) {
            return insets;
        }
        Insets pad = new Insets(0, 0, 0, 0);
        rotateInsets(UIScale.scale(contentMargins), pad, tabPlacement);
        return new Insets(insets.top + pad.top, insets.left + pad.left,
                insets.bottom + pad.bottom, insets.right + pad.right);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        if (isSelected || unselectedBottomColor == null) {
            super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
            return;
        }
        Rectangle r = lowered(tabPlacement, x, y, w, h);
        Paint fill = isHover(tabIndex) && hoverColor != null ? hoverColor
                : LagoonShade.tab(shade, tabPane.getBackground(), unselectedBottomColor, r, tabPlacement);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setPaint(fill);
            g2.fill(outerRounded(tabPlacement, r.x, r.y, r.width, r.height, radius(r)));
        } finally {
            g2.dispose();
        }
    }

    // The outer edge with its rounded corners, the trailing edge, and the leading edge on the first tab only, so
    // neighbours share one line (a later tab's frame starts on the line before it). The edge facing the content
    // belongs to the content frame: the frame is drawn one line past it and clipped there.
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        if (isSelected || unselectedBorderColor == null) {
            super.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
            return;
        }
        Rectangle r = lowered(tabPlacement, x, y, w, h);
        int line = UIScale.scale(1);
        boolean vertical = tabPlacement == LEFT || tabPlacement == RIGHT;
        if (tabIndex > 0) {
            if (vertical) { r.y -= line; r.height += line; } else { r.x -= line; r.width += line; }
        }
        Rectangle frame = new Rectangle(r);
        switch (tabPlacement) {
            case BOTTOM: frame.y -= line; frame.height += line; break;
            case LEFT:   frame.width += line; break;
            case RIGHT:  frame.x -= line; frame.width += line; break;
            default:     frame.height += line; break;
        }
        float radius = radius(r);
        Path2D ring = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        ring.append(outerRounded(tabPlacement, frame.x, frame.y, frame.width, frame.height, radius), false);
        ring.append(outerRounded(tabPlacement, frame.x + line, frame.y + line, frame.width - 2 * line,
                frame.height - 2 * line, Math.max(0, radius - line)), false);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.clip(r);
            g2.setColor(unselectedBorderColor);
            g2.fill(ring);
        } finally {
            g2.dispose();
        }
    }

    /** The corner radius of an unselected tab: half of {@code TabbedPane.cardTabArc}, as the selected tab's. */
    private float radius(Rectangle tab) {
        return Math.min(UIScale.scale(cardTabArc) / 2f, Math.min(tab.width, tab.height) / 2f);
    }

    /** A rectangle with its two corners on the tab's outer edge rounded. */
    private static Shape outerRounded(int tabPlacement, float x, float y, float w, float h, float radius) {
        switch (tabPlacement) {
            case BOTTOM: return FlatUIUtils.createRoundRectanglePath(x, y, w, h, 0, 0, radius, radius);
            case LEFT:   return FlatUIUtils.createRoundRectanglePath(x, y, w, h, radius, 0, radius, 0);
            case RIGHT:  return FlatUIUtils.createRoundRectanglePath(x, y, w, h, 0, radius, 0, radius);
            default:     return FlatUIUtils.createRoundRectanglePath(x, y, w, h, radius, radius, 0, 0);
        }
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        int shift = super.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        if (isSelected) return shift;
        int half = raise() / 2;
        return tabPlacement == LEFT ? shift + half : tabPlacement == RIGHT ? shift - half : shift;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        int shift = super.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        if (isSelected) return shift;
        int half = raise() / 2;
        return tabPlacement == TOP ? shift + half : tabPlacement == BOTTOM ? shift - half : shift;
    }

    // FlatLaf lays out a title and icon here without the label shift (only tab components get it), so an
    // unselected tab's label is moved here, to stay centred in the lowered tab.
    @Override
    protected String layoutAndClipLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title,
                                        Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect,
                                        boolean isSelected) {
        String clipped = super.layoutAndClipLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect,
                iconRect, textRect, isSelected);
        int dx = getTabLabelShiftX(tabPlacement, tabIndex, isSelected) - super.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int dy = getTabLabelShiftY(tabPlacement, tabIndex, isSelected) - super.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.translate(dx, dy);
        textRect.translate(dx, dy);
        return clipped;
    }

    /** An unselected tab's bounds: moved off the outer edge by the raise, the side facing the content kept. */
    private Rectangle lowered(int tabPlacement, int x, int y, int w, int h) {
        int raise = raise();
        switch (tabPlacement) {
            case BOTTOM: return new Rectangle(x, y, w, h - raise);
            case LEFT:   return new Rectangle(x + raise, y, w - raise, h);
            case RIGHT:  return new Rectangle(x, y, w - raise, h);
            default:     return new Rectangle(x, y + raise, w, h - raise);
        }
    }

    private int raise() {
        return UIScale.scale(selectedTabRaise);
    }

    private boolean isHover(int tabIndex) {
        return getRolloverTab() == tabIndex;
    }
}
