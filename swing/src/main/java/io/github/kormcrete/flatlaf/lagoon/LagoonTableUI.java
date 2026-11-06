package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatTableUI;

import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;

/**
 * Tables whose Boolean cells show the theme's {@link LagoonCheckBoxIcon}. FlatLaf gives a table a renderer of its
 * own for Boolean cells, drawn with FlatLaf's larger check box whatever {@code CheckBox.icon} says; this one takes
 * its place, and hands FlatLaf's back when the table is uninstalled, so FlatLaf restores the table's earlier one.
 *
 * <p>With {@code Table.selectionFollowsCellColor} a selected cell keeps the colour its renderer gives it when it is
 * not selected, made more vivid: a colour with a hue more saturated and a little darker, a grey darker. A row or
 * column colour that says what a row is stays readable on the rows being worked on. Cells in the table's own
 * background or alternate row colour, and cells whose renderer already paints its own selection colour, keep
 * what the renderer gave them. {@code false} for the key turns it off for every table; a table's client property
 * of the same name, {@code Boolean.FALSE}, turns it off for that table alone.
 */
public class LagoonTableUI extends FlatTableUI {

    /** The key, and a table's client property, that keep a selected cell's own colour (see the class comment). */
    public static final String SELECTION_FOLLOWS_CELL_COLOR = "Table.selectionFollowsCellColor";

    private TableCellRenderer flatBooleanRenderer;

    public static ComponentUI createUI(JComponent c) {
        return new LagoonTableUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (UIManager.getBoolean(SELECTION_FOLLOWS_CELL_COLOR)) {
            table.remove(rendererPane);
            rendererPane = new ColourKeepingPane();
            table.add(rendererPane);
        }
    }

    /** The selected counterpart of a cell's own colour: more saturated and a little darker; a grey darker still. */
    static Color amplified(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        if (hsb[1] < 0.08f) {
            return Color.getHSBColor(hsb[0], hsb[1], hsb[2] * 0.78f);
        }
        return Color.getHSBColor(hsb[0], Math.min(1f, hsb[1] * 1.55f + 0.12f), hsb[2] * 0.88f);
    }

    /** Paints a selected cell in its own colour, amplified, where the renderer gave it the plain selection colour. */
    private final class ColourKeepingPane extends CellRendererPane {
        @Override
        public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h,
                                   boolean shouldValidate) {
            Point centre = new Point(x + w / 2, y + h / 2);
            int row = table.rowAtPoint(centre);
            int column = table.columnAtPoint(centre);
            if (!Boolean.FALSE.equals(table.getClientProperty(SELECTION_FOLLOWS_CELL_COLOR))
                    && row >= 0 && column >= 0 && table.isCellSelected(row, column)
                    && table.getSelectionBackground().equals(c.getBackground())) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component plain = renderer.getTableCellRendererComponent(table, table.getValueAt(row, column),
                        false, false, row, column);
                Color own = plain.getBackground();
                Color ownForeground = plain.getForeground();
                c = table.prepareRenderer(renderer, row, column);
                if (own != null && !own.equals(table.getBackground())
                        && !own.equals(UIManager.getColor("Table.alternateRowColor"))) {
                    c.setBackground(amplified(own));
                    c.setForeground(ownForeground);
                }
            }
            super.paintComponent(g, c, p, x, y, w, h, shouldValidate);
        }
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        TableCellRenderer installed = table.getDefaultRenderer(Boolean.class);
        // Only the renderer FlatLaf put there: one the application set is not a UIResource and stays.
        if (installed instanceof UIResource && !(installed instanceof BooleanRenderer)) {
            flatBooleanRenderer = installed;
            table.setDefaultRenderer(Boolean.class, new BooleanRenderer());
        }
    }

    @Override
    protected void uninstallDefaults() {
        if (flatBooleanRenderer != null && table.getDefaultRenderer(Boolean.class) instanceof BooleanRenderer) {
            table.setDefaultRenderer(Boolean.class, flatBooleanRenderer);
        }
        flatBooleanRenderer = null;
        super.uninstallDefaults();
    }

    private static final class BooleanRenderer extends DefaultTableCellRenderer implements UIResource {

        private boolean selected;

        BooleanRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            Icon icon = new LagoonCheckBoxIcon() {
                @Override
                protected boolean isSelected(Component c) {
                    return selected;
                }
            };
            setIcon(icon);
            setDisabledIcon(icon);
        }

        @Override
        protected void setValue(Object value) {
            selected = Boolean.TRUE.equals(value);
        }
    }
}
