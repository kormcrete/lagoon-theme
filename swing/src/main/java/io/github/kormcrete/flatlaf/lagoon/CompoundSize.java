package io.github.kormcrete.flatlaf.lagoon;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Dimension;

/**
 * The theme's sizing of a label or button that has text, an icon or both, applied on top of the size
 * Swing's compound-label layout gives. Off by default (FlatLaf's sizes); {@code Lagoon.labelGap} turns on the
 * gap, {@code Lagoon.oddButtonSize} the odd button sizes.
 *
 * <ul>
 *   <li>Text beside an icon, or text alone: the icon-text gap is reserved after the text even when
 *       there is no icon, so a column of labels leaves a little air before the fields next to it.</li>
 *   <li>Text above or below an icon: the gap is reserved twice, once between them and once after.</li>
 *   <li>Buttons are given an odd width and height, so their text and icon centre on a whole pixel.</li>
 * </ul>
 *
 * HTML text is given the same room as plain text, so a label does not shift when its text turns into
 * a link.
 */
final class CompoundSize {

    private CompoundSize() {
    }

    static Dimension adjust(Dimension size, String text, Icon icon, int gap,
                            int horizontalTextPosition, int verticalTextPosition) {
        if (size == null || !UIManager.getBoolean("Lagoon.labelGap")) {
            return size;
        }
        boolean hasText = text != null && !text.isEmpty();
        if (horizontalTextPosition != SwingConstants.CENTER) {
            if (hasText && icon == null) {
                size.width += gap;
            }
        } else if (verticalTextPosition != SwingConstants.CENTER) {
            size.height += gap;
        }
        return size;
    }

    static Dimension odd(Dimension size) {
        if (size != null && UIManager.getBoolean("Lagoon.oddButtonSize")) {
            size.width |= 1;
            size.height |= 1;
        }
        return size;
    }
}
