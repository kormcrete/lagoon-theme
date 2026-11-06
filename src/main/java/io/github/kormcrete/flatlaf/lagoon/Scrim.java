package io.github.kormcrete.flatlaf.lagoon;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;

/**
 * A translucent veil over a window's content with modal content in its middle: a confirmation card, a small
 * form. It covers the whole window in the root pane's layered pane, at the modal layer, and takes the mouse:
 * nothing under it can be clicked, scrolled or hovered. Tab stays within the content.
 *
 * <pre>{@code
 * LagoonCard card = new LagoonCard();
 * card.add(new JLabel("Cancel order ORD-1637?"));
 * Scrim scrim = Scrim.show(frame.getRootPane(), card);
 * ...
 * scrim.close();
 * }</pre>
 *
 * <p>The veil's colour is {@code Scrim.background} (the tokens' {@code scrim}, translucent). Keyboard shortcuts of
 * the window (menu accelerators, mnemonics) are not blocked; the application closes the scrim, e.g. from the
 * content's buttons.
 */
public class Scrim extends JComponent {

    private final JComponent content;
    private JLayeredPane layeredPane;
    private Component previousFocus;
    private final ComponentListener follow = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            fit();
        }
    };

    /** A scrim around {@code content}, shown with {@link #showOn(JRootPane)}. */
    public Scrim(JComponent content) {
        this.content = content;
        setLayout(new GridBagLayout());
        add(content);
        setOpaque(false);
        setFocusCycleRoot(true);
        // listening makes the scrim the target of every mouse event over it
        MouseAdapter swallow = new MouseAdapter() {
        };
        addMouseListener(swallow);
        addMouseMotionListener(swallow);
        addMouseWheelListener(swallow);
        updateUI();
    }

    /** Shows {@code content} over the window of {@code rootPane}. */
    public static Scrim show(JRootPane rootPane, JComponent content) {
        Scrim scrim = new Scrim(content);
        scrim.showOn(rootPane);
        return scrim;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        Color background = getBackground();
        if (background == null || background instanceof UIResource) {
            Color scrim = UIManager.getColor("Scrim.background");
            setBackground(new ColorUIResource(scrim != null ? scrim : new Color(0, 0, 0, 0x6B)));
        }
    }

    /** The content in the middle. */
    public JComponent getContent() {
        return content;
    }

    /** Whether the scrim covers a window: between {@link #showOn(JRootPane)} and {@link #close()}. */
    public boolean isOpen() {
        return layeredPane != null;
    }

    /** Covers the window of {@code rootPane} and moves the focus into the content. */
    public void showOn(JRootPane rootPane) {
        if (layeredPane != null) {
            throw new IllegalStateException("the scrim is already showing");
        }
        layeredPane = rootPane.getLayeredPane();
        previousFocus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        layeredPane.add(this, JLayeredPane.MODAL_LAYER);
        layeredPane.addComponentListener(follow);
        fit();
        SwingUtilities.invokeLater(() -> {
            Component first = getFocusTraversalPolicy().getFirstComponent(this);
            if (first != null) {
                first.requestFocusInWindow();
            }
        });
    }

    /** Takes the scrim away and gives the focus back to where it was. */
    public void close() {
        if (layeredPane == null) {
            return;
        }
        JLayeredPane pane = layeredPane;
        layeredPane = null;
        pane.removeComponentListener(follow);
        pane.remove(this);
        pane.revalidate();
        pane.repaint();
        if (previousFocus != null && previousFocus.isShowing()) {
            previousFocus.requestFocusInWindow();
        }
        previousFocus = null;
    }

    private void fit() {
        if (layeredPane != null) {
            setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
