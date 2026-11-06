package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import com.formdev.flatlaf.ui.FlatMenuBarBorder;
import com.formdev.flatlaf.ui.FlatMenuItemBorder;
import com.formdev.flatlaf.ui.FlatRoundBorder;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;
import com.formdev.flatlaf.ui.FlatTextBorder;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Paint;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * Component borders whose dark frame sits in an etched ring: a shadow line above and to the left, a
 * highlight line below and to the right, in every enabled state; a disabled component has no ring.
 *
 * <p>The ring takes the space FlatLaf reserves as {@code Component.focusWidth} for its focus ring,
 * which these borders never paint. Focus shows on the frame of a field, combo box or spinner instead: it
 * takes {@code Component.focusedBorderColor} and thickens inwards by {@code Component.innerFocusWidth}.
 * Colours: {@code Component.ringShadowColor} and {@code Component.ringHighlightColor}. The properties file names
 * these classes as the {@code *.border} of fields, combo boxes, spinners, buttons and scroll panes;
 * {@link MenuBar} is the menu bar's and {@link MenuItem} a popup menu item's, which have no ring.
 */
public final class LagoonBorders {

    private LagoonBorders() {
    }

    static void paintRing(Component c, Graphics g, int x, int y, int width, int height, int focusWidth) {
        Color shadow = UIManager.getColor("Component.ringShadowColor");
        Color highlight = UIManager.getColor("Component.ringHighlightColor");
        if (shadow == null || highlight == null || focusWidth <= 0 || !c.isEnabled()) {
            return;
        }
        int t = scale(focusWidth);
        g.setColor(shadow);
        g.fillRect(x, y, width - t, t);
        g.fillRect(x, y + t, t, height - 2 * t);
        g.setColor(highlight);
        g.fillRect(x + width - t, y, t, height);
        g.fillRect(x, y + height - t, width - t, t);
    }

    /**
     * Text fields, password fields and formatted text fields. A field editing a table or tree cell keeps the
     * frame, ring and insets it has anywhere else, where FlatLaf strips them: the text then sits where it sits
     * in the cell before editing starts.
     */
    public static class Text extends FlatTextBorder {
        @Override
        protected Color getFocusColor(Component c) {
            return null;
        }

        @Override
        protected boolean isCellEditor(Component c) {
            return false;
        }

        @Override
        protected float getBorderWidth(Component c) {
            return super.getBorderWidth(c) + (isFocused(c) ? innerFocusWidth : 0);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            paintRing(c, g, x, y, width, height, getFocusWidth(c));
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    /** Combo boxes and spinners. */
    public static class Round extends FlatRoundBorder {
        @Override
        protected Color getFocusColor(Component c) {
            return null;
        }

        @Override
        protected float getBorderWidth(Component c) {
            return super.getBorderWidth(c) + (isFocused(c) ? innerFocusWidth : 0);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            paintRing(c, g, x, y, width, height, getFocusWidth(c));
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    /**
     * Scroll panes. One around a text component is that input's edge and framed like a field. One around
     * a table, list, tree or panel shows content: a plain line ({@code ScrollPane.borderColor}, a tab's
     * content frame by default) without a ring, in the space the ring takes elsewhere.
     */
    public static class ScrollPane extends FlatScrollPaneBorder {
        @Override
        protected Color getFocusColor(Component c) {
            return null;
        }

        @Override
        protected Paint getBorderColor(Component c) {
            Color line = UIManager.getColor("ScrollPane.borderColor");
            return line == null || framesText(c) ? super.getBorderColor(c) : line;
        }

        @Override
        protected float getBorderWidth(Component c) {
            return super.getBorderWidth(c) + (framesText(c) && isFocused(c) ? innerFocusWidth : 0);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (framesText(c)) {
                paintRing(c, g, x, y, width, height, getFocusWidth(c));
            }
            super.paintBorder(c, g, x, y, width, height);
        }

        private static boolean framesText(Component c) {
            JViewport viewport = c instanceof JScrollPane ? ((JScrollPane) c).getViewport() : null;
            return viewport != null && viewport.getView() instanceof JTextComponent;
        }
    }

    /** Buttons and toggle buttons; only those that paint a frame get a ring, never a toolbar button. */
    public static class Button extends FlatButtonBorder {
        @Override
        protected Color getFocusColor(Component c) {
            return null;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (paintsFrame(c)) {
                paintRing(c, g, x, y, width, height, getFocusWidth(c));
            }
            super.paintBorder(c, g, x, y, width, height);
        }

        /** The cases in which {@link FlatButtonBorder} paints its frame, from public API. */
        static boolean paintsFrame(Component c) {
            if (!(c instanceof AbstractButton) || !((AbstractButton) c).isContentAreaFilled()
                    || c.getParent() instanceof JToolBar) {
                return false;
            }
            Object type = ((JComponent) c).getClientProperty(FlatClientProperties.BUTTON_TYPE);
            return type == null || !(FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON.equals(type)
                    || FlatClientProperties.BUTTON_TYPE_BORDERLESS.equals(type)
                    || FlatClientProperties.BUTTON_TYPE_HELP.equals(type)
                    || FlatClientProperties.BUTTON_TYPE_TAB.equals(type));
        }
    }

    /** The menu bar: FlatLaf's bottom line, with {@code MenuBar.padding} added around the items. */
    public static class MenuBar extends FlatMenuBarBorder {
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets = super.getBorderInsets(c, insets);
            Insets padding = UIManager.getInsets("MenuBar.padding");
            if (padding != null) {
                insets.top += scale(padding.top);
                insets.left += scale(padding.left);
                insets.bottom += scale(padding.bottom);
                insets.right += scale(padding.right);
            }
            return insets;
        }
    }

    /**
     * An item of a popup menu. Its text starts {@code MenuItem.textInset} in when no item of the popup has an
     * icon; when one does, every item's icon column starts {@code MenuItem.iconInset} in, so the text of all
     * items lines up behind it. An item with an icon is a pixel taller. Top, bottom and right come from the
     * margin; an item whose margin the application set keeps FlatLaf's border.
     *
     * <p>A menu bar's open menu is framed in {@code MenuBar.selectionBorderColor} on three sides, so it reads as
     * the head of its popup; the popup's own frame closes it below.
     */
    public static class MenuItem extends FlatMenuItemBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            super.paintBorder(c, g, x, y, width, height);
            Color frame = UIManager.getColor("MenuBar.selectionBorderColor");
            if (frame != null && c instanceof JMenu && c.getParent() instanceof JMenuBar && ((JMenu) c).isSelected()) {
                int line = scale(1);
                g.setColor(frame);
                g.fillRect(x, y, width, line);
                g.fillRect(x, y, line, height);
                g.fillRect(x + width - line, y, line, height);
            }
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets = super.getBorderInsets(c, insets);
            if (!(c instanceof JMenuItem) || !(c.getParent() instanceof JPopupMenu)
                    || !(((JMenuItem) c).getMargin() instanceof UIResource)) {
                return insets;
            }
            JMenuItem item = (JMenuItem) c;
            if (anyIcon(c.getParent())) {
                insets.left = scale(UIManager.getInt("MenuItem.iconInset"));
            } else {
                // FlatLaf's icon column is the item's check mark, or empty, followed by the icon-text gap
                Icon check = checkIcon(item);
                int column = check != null ? check.getIconWidth() : 0;
                insets.left = scale(UIManager.getInt("MenuItem.textInset")) - column - scale(item.getIconTextGap());
            }
            if (item.getIcon() != null) {
                insets.bottom += scale(1);
            }
            return insets;
        }

        private static boolean anyIcon(Container popup) {
            for (Component c : popup.getComponents()) {
                if (c instanceof JMenuItem && ((JMenuItem) c).getIcon() != null) {
                    return true;
                }
            }
            return false;
        }

        private static Icon checkIcon(JMenuItem item) {
            if (item instanceof JCheckBoxMenuItem) {
                return UIManager.getIcon("CheckBoxMenuItem.checkIcon");
            }
            if (item instanceof JRadioButtonMenuItem) {
                return UIManager.getIcon("RadioButtonMenuItem.checkIcon");
            }
            return null;
        }
    }
}
