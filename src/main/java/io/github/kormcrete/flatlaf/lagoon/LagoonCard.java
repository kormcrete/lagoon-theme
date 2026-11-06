package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;

/**
 * Lagoon's main surface: a white panel with soft corners on a soft shadow, to sit on the ground colour.
 *
 * <p>The card is not opaque: around its rounded corners and under its shadow the parent shows through. Its
 * bounds include the shadow ({@code LagoonCard.shadowWidth} on each side, {@code LagoonCard.shadowOffset} more
 * below, less above), so cards laid out side by side keep their shadows. Inside, the content keeps
 * {@code LagoonCard.padding} (the tokens' gap) from the edge; change it with {@link #setPadding(Insets)}, not with
 * a border of one's own.
 *
 * <p>UI defaults: {@code LagoonCard.background}, {@code LagoonCard.arc}, {@code LagoonCard.padding},
 * {@code LagoonCard.shadowColor} (its alpha is the shadow's darkness where it meets the card),
 * {@code LagoonCard.shadowWidth}, {@code LagoonCard.shadowOffset}, and an optional hairline
 * {@code LagoonCard.borderColor}.
 */
public class LagoonCard extends JPanel {

    private Insets padding;

    /** A card with a {@link BorderLayout}. */
    public LagoonCard() {
        this(new BorderLayout());
    }

    public LagoonCard(LayoutManager layout) {
        super(layout);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        Color background = getBackground();
        if (background == null || background instanceof UIResource) {
            Color card = UIManager.getColor("LagoonCard.background");
            setBackground(new ColorUIResource(card != null ? card : Color.WHITE));
        }
        setBorder(new CardBorder());
    }

    /** The space between the card's edge and its content; null for {@code LagoonCard.padding}. */
    public Insets getPadding() {
        return padding != null ? (Insets) padding.clone() : null;
    }

    /** Sets the space between the card's edge and its content (unscaled); null for {@code LagoonCard.padding}. */
    public void setPadding(Insets padding) {
        Insets old = this.padding;
        this.padding = padding != null ? (Insets) padding.clone() : null;
        firePropertyChange("padding", old, padding);
        revalidate();
        repaint();
    }

    /** The shadow's width, scaled. */
    static int shadowWidth() {
        return UIScale.scale(Math.max(0, UIManager.getInt("LagoonCard.shadowWidth")));
    }

    static int shadowOffset() {
        return UIScale.scale(Math.max(0, UIManager.getInt("LagoonCard.shadowOffset")));
    }

    /** The space the shadow takes around the card face, scaled. */
    static Insets shadowInsets() {
        int width = shadowWidth();
        int offset = Math.min(shadowOffset(), width);
        return new Insets(width - offset, width, width + offset, width);
    }

    /** The card face: the bounds less the shadow. */
    RoundRectangle2D face() {
        Insets s = shadowInsets();
        float arc = UIScale.scale((float) UIManager.getInt("LagoonCard.arc"));
        return new RoundRectangle2D.Float(s.left, s.top,
                getWidth() - s.left - s.right, getHeight() - s.top - s.bottom, arc, arc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            RoundRectangle2D face = face();
            paintShadow(g2, face);
            g2.setColor(getBackground());
            g2.fill(face);
            Color line = UIManager.getColor("LagoonCard.borderColor");
            if (line != null) {
                g2.setColor(line);
                float w = UIScale.scale(1f);
                g2.draw(new RoundRectangle2D.Double(face.getX() + w / 2, face.getY() + w / 2,
                        face.getWidth() - w, face.getHeight() - w, face.getArcWidth() - w, face.getArcHeight() - w));
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    /**
     * A soft shadow: rings around the face, lowered by the offset, each a little lighter outwards, so the darkness
     * fades from the shadow colour's alpha at the face to nothing at the shadow's width.
     */
    private static void paintShadow(Graphics2D g, RoundRectangle2D face) {
        Color color = UIManager.getColor("LagoonCard.shadowColor");
        int width = shadowWidth();
        if (color == null || color.getAlpha() == 0 || width <= 0) {
            return;
        }
        int offset = Math.min(shadowOffset(), width);
        float darkness = color.getAlpha() / 255f;
        g.setColor(new Color(color.getRGB() & 0xFFFFFF));
        float squared = (float) width * width;
        for (int k = width; k >= 1; k--) {
            // the rings from k outwards add up to darkness * (1 - (k - 1) / width)^2
            float alpha = darkness * (2 * (width - k) + 1) / squared;
            g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, alpha)));
            g.fill(new RoundRectangle2D.Double(face.getX() - k, face.getY() - k + offset,
                    face.getWidth() + 2 * k, face.getHeight() + 2 * k,
                    face.getArcWidth() + 2 * k, face.getArcHeight() + 2 * k));
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    /** The shadow's room plus the padding. */
    private final class CardBorder extends AbstractBorder implements UIResource {
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets s = shadowInsets();
            Insets p = padding != null ? padding : UIManager.getInsets("LagoonCard.padding");
            if (p == null) {
                p = new Insets(0, 0, 0, 0);
            }
            insets.top = s.top + UIScale.scale(p.top);
            insets.left = s.left + UIScale.scale(p.left);
            insets.bottom = s.bottom + UIScale.scale(p.bottom);
            insets.right = s.right + UIScale.scale(p.right);
            return insets;
        }
    }
}
