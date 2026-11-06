package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A tinted rounded square holding initials, a short code or an icon: customers, products, quantities.
 *
 * <p>Its colours, unless given with {@link #setTileColors(Color, Color)} or {@link #setTone(LagoonTone)}, are one of
 * the tokens' tile pairs ({@code Lagoon.tile.<i>.background} and {@code .foreground}), picked by a key: the same key
 * always gets the same pair, here and in the Compose theme. The corners are {@code AvatarTile.arc}, the text
 * {@code AvatarTile.font}, grown with the tile (its size at a 36-pixel tile).
 */
public class AvatarTile extends JComponent {

    /** The default size, unscaled: 40 pixels, as in the Compose theme. */
    public static final int DEFAULT_SIZE = 40;

    private String text;
    private Icon icon;
    private String key;
    private int tileSize = DEFAULT_SIZE;
    private Color tileBackground;
    private Color tileForeground;
    private LagoonTone tone;

    /** A tile showing {@code text}, coloured by the text itself. */
    public AvatarTile(String text) {
        this(text, text);
    }

    /** A tile showing {@code text}, coloured by {@code key} (a customer's id, say). */
    public AvatarTile(String text, String key) {
        this.text = text;
        this.key = key;
        updateUI();
    }

    /** A tile showing an icon in the tone's colours. */
    public AvatarTile(Icon icon, LagoonTone tone) {
        this.icon = icon;
        this.tone = tone;
        this.key = "";
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        Font font = getFont();
        if (font == null || font instanceof UIResource) {
            Font tileFont = UIManager.getFont("AvatarTile.font");
            if (tileFont == null) {
                tileFont = UIManager.getFont("Label.font");
            }
            setFont(tileFont != null ? new FontUIResource(tileFont) : null);
        }
    }

    /** The index of the tile pair for {@code key} among {@code count}: Compose's {@code LagoonColors.tile}. */
    public static int tileIndex(String key, int count) {
        return (key.hashCode() & Integer.MAX_VALUE) % count;
    }

    public String getText() { return text; }

    public void setText(String text) {
        String old = this.text;
        this.text = text;
        firePropertyChange("text", old, text);
        repaint();
    }

    public Icon getIcon() { return icon; }

    public void setIcon(Icon icon) {
        Icon old = this.icon;
        this.icon = icon;
        firePropertyChange("icon", old, icon);
        repaint();
    }

    public String getKey() { return key; }

    /** The key that picks the tile colours. */
    public void setKey(String key) {
        String old = this.key;
        this.key = key;
        firePropertyChange("key", old, key);
        repaint();
    }

    /** The tile's side, unscaled. */
    public int getTileSize() { return tileSize; }

    public void setTileSize(int tileSize) {
        int old = this.tileSize;
        this.tileSize = tileSize;
        firePropertyChange("tileSize", old, tileSize);
        revalidate();
        repaint();
    }

    public LagoonTone getTone() { return tone; }

    /** Colours the tile in a tone's tint and ink instead of a tile pair; null for the tile pair. */
    public void setTone(LagoonTone tone) {
        LagoonTone old = this.tone;
        this.tone = tone;
        firePropertyChange("tone", old, tone);
        repaint();
    }

    /** Colours of the tile's own, over a tone and the tile pairs; null for those. */
    public void setTileColors(Color background, Color foreground) {
        this.tileBackground = background;
        this.tileForeground = foreground;
        repaint();
    }

    /** The colour the tile is filled with. */
    public Color getTileBackground() {
        return colors()[0];
    }

    /** The colour of the text on the tile. */
    public Color getTileForeground() {
        return colors()[1];
    }

    private Color[] colors() {
        if (tileBackground != null && tileForeground != null) {
            return new Color[] {tileBackground, tileForeground};
        }
        if (tone != null && tone.background() != null) {
            return new Color[] {tone.background(), tone.foreground()};
        }
        int count = UIManager.getInt("Lagoon.tileCount");
        if (count > 0) {
            int i = tileIndex(key != null ? key : "", count);
            Color bg = UIManager.getColor("Lagoon.tile." + i + ".background");
            Color fg = UIManager.getColor("Lagoon.tile." + i + ".foreground");
            if (bg != null && fg != null) {
                return new Color[] {bg, fg};
            }
        }
        // outside the modern Lagoon
        return new Color[] {UIManager.getColor("Panel.background"), UIManager.getColor("Label.foreground")};
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        int side = UIScale.scale(tileSize);
        return new Dimension(side, side);
    }

    @Override
    public Dimension getMinimumSize() {
        return isMinimumSizeSet() ? super.getMinimumSize() : getPreferredSize();
    }

    /** The text's font: {@link #getFont()} grown or shrunk with the tile, as in the Compose theme. */
    Font tileFont() {
        Font font = getFont();
        return font.deriveFont(font.getSize2D() * tileSize / 36f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            Color[] colors = colors();
            int side = Math.min(getWidth(), getHeight());
            int x = (getWidth() - side) / 2;
            int y = (getHeight() - side) / 2;
            float arc = Math.min(side, UIScale.scale((float) UIManager.getInt("AvatarTile.arc")));
            g2.setColor(colors[0]);
            g2.fill(new RoundRectangle2D.Float(x, y, side, side, arc, arc));
            if (icon != null) {
                icon.paintIcon(this, g2, x + (side - icon.getIconWidth()) / 2, y + (side - icon.getIconHeight()) / 2);
            } else if (text != null && !text.isEmpty()) {
                g2.setFont(tileFont());
                g2.setColor(colors[1]);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (side - fm.stringWidth(text)) / 2;
                int ty = y + (side - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
            }
        } finally {
            g2.dispose();
        }
    }
}
