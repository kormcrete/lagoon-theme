package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A quantity stepper: − 3 +, in a pill. The buttons stop at the minimum and the maximum; the arrow keys step too
 * while the stepper has the focus.
 *
 * <p>UI defaults: {@code Stepper.background} and {@code Stepper.foreground}, {@code Stepper.filledBackground} and
 * {@code Stepper.filledForeground} (a {@linkplain #setFilled(boolean) filled} stepper, as in a product list),
 * {@code Stepper.minimumHeight} (the tokens' touch target) and {@code Stepper.font}.
 */
public class Stepper extends JComponent {

    private final JButton decrement = stepButton(false);
    private final JButton increment = stepButton(true);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private int value;
    private int minimum;
    private int maximum;
    private boolean filled;

    /** A stepper from 0 up. */
    public Stepper() {
        this(0, 0, Integer.MAX_VALUE);
    }

    public Stepper(int value, int minimum, int maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum " + minimum + " > maximum " + maximum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = clamp(value);
        setLayout(new StepperLayout());
        add(decrement);
        add(label);
        add(increment);
        decrement.addActionListener(e -> setValue(this.value - 1));
        increment.addActionListener(e -> setValue(this.value + 1));
        decrement.getAccessibleContext().setAccessibleName("One less");
        increment.getAccessibleContext().setAccessibleName("One more");
        bindKeys();
        updateUI();
        update();
    }

    private static JButton stepButton(boolean plus) {
        JButton button = new JButton();
        StepIcon icon = new StepIcon(plus);
        button.setIcon(icon);
        button.setDisabledIcon(icon);
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        return button;
    }

    private void bindKeys() {
        String less = "Stepper.decrement";
        String more = "Stepper.increment";
        for (int key : new int[] {KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT}) {
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key, 0), less);
        }
        for (int key : new int[] {KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_PLUS, KeyEvent.VK_ADD}) {
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key, 0), more);
        }
        getActionMap().put(less, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setValue(value - 1);
            }
        });
        getActionMap().put(more, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setValue(value + 1);
            }
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        Font font = UIManager.getFont("Stepper.font");
        // plain fonts and colours: the children's own updateUI must not take them back
        if (font != null) {
            label.setFont(font.deriveFont(font.getStyle()));
        }
        applyColors();
    }

    private void applyColors() {
        Color fg = color(filled ? "Stepper.filledForeground" : "Stepper.foreground", "Label.foreground");
        for (JComponent c : new JComponent[] {decrement, increment, label}) {
            c.setForeground(new Color(fg.getRGB(), true));
        }
        // hover and press as a veil of the ink, so they show on the plain and the filled pill alike
        String style = "toolbar.hoverBackground: " + FlatLagoonModernLaf.hex(withAlpha(fg, 0.10f))
                + "; toolbar.pressedBackground: " + FlatLagoonModernLaf.hex(withAlpha(fg, 0.18f));
        decrement.putClientProperty(FlatClientProperties.STYLE, style);
        increment.putClientProperty(FlatClientProperties.STYLE, style);
        repaint();
    }

    private static Color withAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(alpha * 255));
    }

    private static Color color(String key, String fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : UIManager.getColor(fallback);
    }

    public int getValue() { return value; }

    /** Sets the value, kept between the minimum and the maximum; tells the change listeners if it changed. */
    public void setValue(int value) {
        int old = this.value;
        this.value = clamp(value);
        if (old != this.value) {
            update();
            firePropertyChange("value", old, this.value);
            fireStateChanged();
        }
    }

    public int getMinimum() { return minimum; }

    public void setMinimum(int minimum) {
        this.minimum = Math.min(minimum, maximum);
        setValue(value);
        update();
    }

    public int getMaximum() { return maximum; }

    public void setMaximum(int maximum) {
        this.maximum = Math.max(maximum, minimum);
        setValue(value);
        update();
    }

    public boolean isFilled() { return filled; }

    /** A filled stepper is in the accent colour. */
    public void setFilled(boolean filled) {
        boolean old = this.filled;
        this.filled = filled;
        applyColors();
        firePropertyChange("filled", old, filled);
    }

    /** The − button. */
    public JButton getDecrementButton() { return decrement; }

    /** The + button. */
    public JButton getIncrementButton() { return increment; }

    /** The label showing the value. */
    public JLabel getValueLabel() { return label; }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        update();
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
            l.stateChanged(event);
        }
    }

    private int clamp(int v) {
        return Math.max(minimum, Math.min(maximum, v));
    }

    private void update() {
        label.setText(String.valueOf(value));
        decrement.setEnabled(isEnabled() && value > minimum);
        increment.setEnabled(isEnabled() && value < maximum);
        label.setEnabled(isEnabled());
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            g2.setColor(color(filled ? "Stepper.filledBackground" : "Stepper.background", "Panel.background"));
            float h = getHeight();
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), h, h, h));
        } finally {
            g2.dispose();
        }
    }

    /** The height of the pill, scaled. */
    static int pillHeight() {
        int height = UIManager.getInt("Stepper.minimumHeight");
        return UIScale.scale(height > 0 ? height : 30);
    }

    /** − value +: square-ish buttons the pill's height tall, the value between them. */
    private final class StepperLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        private int buttonWidth(int height) {
            return Math.round(height * 0.95f);
        }

        /** Wide enough for the widest value, so stepping does not move the buttons. */
        private int labelWidth() {
            FontMetrics fm = label.getFontMetrics(label.getFont());
            int widest = Math.max(fm.stringWidth(String.valueOf(minimum)), fm.stringWidth(String.valueOf(value)));
            if (maximum != Integer.MAX_VALUE) {
                widest = Math.max(widest, fm.stringWidth(String.valueOf(maximum)));
            }
            Insets in = label.getInsets();
            return Math.max(UIScale.scale(22), widest + in.left + in.right);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int height = Math.max(pillHeight(), label.getPreferredSize().height);
            Insets in = parent.getInsets();
            return new Dimension(in.left + 2 * buttonWidth(height) + labelWidth() + in.right,
                    in.top + height + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int height = parent.getHeight() - in.top - in.bottom;
            int width = parent.getWidth() - in.left - in.right;
            int button = Math.min(buttonWidth(height), width / 3);
            decrement.setBounds(in.left, in.top, button, height);
            label.setBounds(in.left + button, in.top, width - 2 * button, height);
            increment.setBounds(in.left + width - button, in.top, button, height);
        }
    }

    /** A minus or plus in the button's foreground; faded when the button is disabled. */
    private static final class StepIcon implements Icon {
        private final boolean plus;

        StepIcon(boolean plus) {
            this.plus = plus;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                FlatUIUtils.setRenderingHints(g2);
                if (!c.isEnabled()) {
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.35f));
                }
                g2.setColor(c.getForeground());
                float size = getIconWidth();
                float stroke = UIScale.scale(1.75f);
                g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                float mid = size / 2f;
                float inset = stroke;
                g2.draw(new Line2D.Float(x + inset, y + mid, x + size - inset, y + mid));
                if (plus) {
                    g2.draw(new Line2D.Float(x + mid, y + inset, x + mid, y + size - inset));
                }
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return Math.round(pillHeight() * 0.4f);
        }

        @Override
        public int getIconHeight() {
            return getIconWidth();
        }
    }
}
