package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatTableHeaderUI;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Enumeration;

/**
 * Table headers as tall as their text while the theme's own renderer draws them: the cell margins give the text room
 * at the sides, and above and below they lie on the header's lines rather than adding to its height. A header with a
 * renderer of the application's keeps the height that renderer asks for. {@code TableHeader.height} stays a minimum.
 */
public class LagoonTableHeaderUI extends FlatTableHeaderUI {

    public static ComponentUI createUI(JComponent c) {
        return new LagoonTableHeaderUI();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        Insets cell = themeCellInsets();
        if (size.height > 0 && cell != null) {
            size.height = Math.max(size.height - cell.top - cell.bottom, UIScale.scale(height));
        }
        return size;
    }

    /** The insets of a cell the theme's renderer draws; {@code null} when the application's renderer draws any. */
    private Insets themeCellInsets() {
        for (Enumeration<TableColumn> columns = header.getColumnModel().getColumns(); columns.hasMoreElements(); ) {
            if (columns.nextElement().getHeaderRenderer() != null) {
                return null;
            }
        }
        if (header.getTable() == null) {
            return null;
        }
        // The file chooser wraps the theme's renderer in one of its own, which hands back the theme's component.
        Component cell = header.getDefaultRenderer().getTableCellRendererComponent(header.getTable(), "", false, false,
                -1, 0);
        return cell instanceof UIResource && cell instanceof JComponent ? ((JComponent) cell).getInsets() : null;
    }
}
