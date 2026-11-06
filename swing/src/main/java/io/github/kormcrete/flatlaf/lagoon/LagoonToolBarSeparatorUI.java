package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.ui.FlatToolBarSeparatorUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Dimension;

/**
 * Toolbar separators that keep their line in a toolbar laid out by preferred sizes, such as a
 * FlowLayout. FlatLaf's preferred size has no length along the line, which only a BoxLayout stretches;
 * here it is {@code ToolBar.separatorLength}, and the width is {@code ToolBar.separatorWidth} as given.
 * A size set on the separator itself still wins.
 *
 * <p>Not done with {@code ToolBar.separatorSize}: Swing stores that on each separator turned the other
 * way round and as a plain value, so it would outlive a switch to another look and feel.
 */
public class LagoonToolBarSeparatorUI extends FlatToolBarSeparatorUI {

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c)
                ? FlatUIUtils.createSharedUI(LagoonToolBarSeparatorUI.class, () -> new LagoonToolBarSeparatorUI(true))
                : new LagoonToolBarSeparatorUI(false);
    }

    protected LagoonToolBarSeparatorUI(boolean shared) {
        super(shared);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        JToolBar.Separator separator = (JToolBar.Separator) c;
        if (separator.getSeparatorSize() != null) {
            return super.getPreferredSize(c);
        }
        int width = UIScale.scale(UIManager.getInt("ToolBar.separatorWidth"));
        int length = UIScale.scale(UIManager.getInt("ToolBar.separatorLength"));
        return separator.getOrientation() == SwingConstants.VERTICAL
                ? new Dimension(width, length)
                : new Dimension(length, width);
    }
}
