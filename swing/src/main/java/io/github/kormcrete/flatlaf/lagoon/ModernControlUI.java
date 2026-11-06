package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatComboBoxUI;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatFormattedTextFieldUI;
import com.formdev.flatlaf.ui.FlatListUI;
import com.formdev.flatlaf.ui.FlatPasswordFieldUI;
import com.formdev.flatlaf.ui.FlatSpinnerUI;
import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatToggleButtonUI;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Dimension;
import java.awt.LayoutManager;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * UI delegates of {@link FlatLagoonModernLaf} that make controls and rows as tall as the tokens say: buttons
 * (icon-only ones as squares), text fields, combo boxes and spinners {@code Button.minimumHeight},
 * {@code TextField.minimumHeight} and the like ({@code controlHeight}); list rows at least {@code List.rowHeight}
 * ({@code rowHeight}, as table and tree rows). Otherwise FlatLaf's delegates.
 *
 * <p>A component's own minimum, FlatLaf's client property {@code JComponent.minimumHeight}, wins. Only controls in
 * the theme's frame get the height: not a field inside a spinner or combo box, not a table's cell editor, not a
 * toolbar button.
 */
public final class ModernControlUI {

    private ModernControlUI() {
    }

    /** The preferred size, at least {@code minimumHeight} (unscaled) tall plus the focus ring, for a framed control. */
    static Dimension atLeast(JComponent c, Dimension size, int minimumHeight) {
        if (size == null || !(c.getBorder() instanceof FlatBorder)) {
            return size;
        }
        int floor = scale(FlatUIUtils.minimumHeight(c, minimumHeight))
                + Math.round(FlatUIUtils.getBorderFocusWidth(c) * 2);
        if (size.height < floor) {
            size = new Dimension(size.width, floor);
        }
        return size;
    }

    /**
     * FlatLaf skips the minimum height of a button showing only an icon; such a framed button is made as tall as the
     * other controls, and square. Not toolbar buttons, nor buttons of a FlatLaf button type (borderless, help, tab...).
     */
    static Dimension iconOnlyAtLeast(AbstractButton b, Dimension size, int minimumHeight) {
        boolean iconOnly = b.getIcon() != null && (b.getText() == null || b.getText().isEmpty());
        if (size == null || !iconOnly || b.getParent() instanceof JToolBar
                || b.getClientProperty(FlatClientProperties.BUTTON_TYPE) != null) {
            return size;
        }
        Dimension tall = atLeast(b, size, minimumHeight);
        return tall == size ? size : new Dimension(Math.max(tall.width, tall.height), tall.height);
    }

    /** Buttons: FlatLaf applies {@code minimumHeight}, but reads it only from styles; this reads the UI default. */
    public static class Button extends FlatButtonUI {
        public static ComponentUI createUI(JComponent c) {
            return new Button();
        }

        protected Button() {
            super(false);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return iconOnlyAtLeast((AbstractButton) c, super.getPreferredSize(c), minimumHeight);
        }

        @Override
        protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            minimumHeight = UIManager.getInt(getPropertyPrefix() + "minimumHeight");
            LagoonButtonUI.keepArrowButtonBorderless(b);
        }
    }

    public static class ToggleButton extends FlatToggleButtonUI {
        public static ComponentUI createUI(JComponent c) {
            return new ToggleButton();
        }

        protected ToggleButton() {
            super(false);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return iconOnlyAtLeast((AbstractButton) c, super.getPreferredSize(c), minimumHeight);
        }

        @Override
        protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            minimumHeight = UIManager.getInt(getPropertyPrefix() + "minimumHeight");
        }
    }

    public static class TextField extends FlatTextFieldUI {
        public static ComponentUI createUI(JComponent c) {
            return new TextField();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return atLeast(c, super.getPreferredSize(c), UIManager.getInt(getPropertyPrefix() + ".minimumHeight"));
        }
    }

    public static class PasswordField extends FlatPasswordFieldUI {
        public static ComponentUI createUI(JComponent c) {
            return new PasswordField();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return atLeast(c, super.getPreferredSize(c), UIManager.getInt(getPropertyPrefix() + ".minimumHeight"));
        }
    }

    public static class FormattedTextField extends FlatFormattedTextFieldUI {
        public static ComponentUI createUI(JComponent c) {
            return new FormattedTextField();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return atLeast(c, super.getPreferredSize(c), UIManager.getInt(getPropertyPrefix() + ".minimumHeight"));
        }
    }

    public static class ComboBox extends FlatComboBoxUI {
        public static ComponentUI createUI(JComponent c) {
            return new ComboBox();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return atLeast(c, super.getPreferredSize(c), UIManager.getInt("ComboBox.minimumHeight"));
        }
    }

    public static class Spinner extends FlatSpinnerUI {
        public static ComponentUI createUI(JComponent c) {
            return new Spinner();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension size = super.getPreferredSize(c);
            if (size == null) {
                // Basic's spinner leaves its size to the layout
                LayoutManager layout = c.getLayout();
                size = layout != null ? layout.preferredLayoutSize(c) : null;
            }
            return atLeast(c, size, UIManager.getInt("Spinner.minimumHeight"));
        }
    }

    /** Lists: every row at least {@code List.rowHeight} tall, unless the list fixes its own cell height. */
    public static class List extends FlatListUI {
        public static ComponentUI createUI(JComponent c) {
            return new List();
        }

        @Override
        protected void updateLayoutState() {
            super.updateLayoutState();
            if (list.getFixedCellHeight() != -1) {
                return;
            }
            int floor = scale(UIManager.getInt("List.rowHeight"));
            if (cellHeights != null) {
                for (int i = 0; i < cellHeights.length; i++) {
                    cellHeights[i] = Math.max(cellHeights[i], floor);
                }
            } else {
                cellHeight = Math.max(cellHeight, floor);
            }
        }
    }
}
