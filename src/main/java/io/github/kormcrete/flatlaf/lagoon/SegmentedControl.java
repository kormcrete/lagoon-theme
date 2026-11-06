package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Options in one rounded track, the selected one a white segment: "All 12 · Placed 4 · Shipped 8".
 *
 * <p>The track is this panel in the style class {@code segmented} ({@code [style]Panel.segmented}), the options are
 * toggle buttons in the style class of the same name ({@code [style]ToggleButton.segmented}). Together they are the
 * tokens' {@code controlHeight} tall. Unselected options are in {@code SegmentedControl.font}, the selected one in
 * {@code SegmentedControl.selectedFont}; each option is as wide as its widest look, so selecting does not move them.
 */
public class SegmentedControl extends JPanel {

    private final List<Segment> segments = new ArrayList<>();
    private final ButtonGroup group = new ButtonGroup();
    private int selectedIndex = -1;

    /** A control with these options, the first one selected. */
    public SegmentedControl(String... options) {
        super(null);
        setLayout(new RowLayout());
        putClientProperty(FlatClientProperties.STYLE_CLASS, "segmented");
        for (String option : options) {
            Segment segment = new Segment(option);
            int index = segments.size();
            segment.addItemListener(e -> {
                if (segment.isSelected()) {
                    select(index);
                }
            });
            segments.add(segment);
            group.add(segment);
            add(segment);
        }
        updateSegments();
        if (!segments.isEmpty()) {
            setSelectedIndex(0);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateSegments();
    }

    /** Heights and fonts; done again when the look-and-feel changes. */
    private void updateSegments() {
        if (segments == null) {
            return; // the JPanel constructor's updateUI, before the fields are set
        }
        // the segments fill the track's height: controlHeight less the track's insets (unscaled)
        Insets track = getInsets();
        int height = UIManager.getInt("Lagoon.metrics.controlHeight")
                - Math.round(UIScale.unscale((float) (track.top + track.bottom)));
        for (Segment s : segments) {
            s.putClientProperty(FlatClientProperties.MINIMUM_HEIGHT, Math.max(0, height));
            s.updateFont();
        }
        revalidate();
    }

    /** The number of options. */
    public int getOptionCount() {
        return segments.size();
    }

    /** The toggle button of option {@code index}. */
    public JToggleButton getSegment(int index) {
        return segments.get(index);
    }

    /** The options' texts. */
    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        for (Segment s : segments) {
            options.add(s.getText());
        }
        return Collections.unmodifiableList(options);
    }

    /** The selected option, or -1 for none. */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /** Selects an option (-1 for none); tells the change listeners if the selection changed. */
    public void setSelectedIndex(int index) {
        if (index < -1 || index >= segments.size()) {
            throw new IndexOutOfBoundsException("option " + index + " of " + segments.size());
        }
        if (index == -1) {
            group.clearSelection();
            select(-1);
        } else {
            segments.get(index).setSelected(true);
        }
    }

    private void select(int index) {
        int old = selectedIndex;
        if (old == index) {
            return;
        }
        selectedIndex = index;
        for (Segment s : segments) {
            s.updateFont();
        }
        revalidate();
        repaint();
        firePropertyChange("selectedIndex", old, index);
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
            l.stateChanged(event);
        }
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /** An option: a toggle button as wide as its text in the wider of the two fonts. */
    private static final class Segment extends JToggleButton {
        Segment(String text) {
            super(text);
            putClientProperty(FlatClientProperties.STYLE_CLASS, "segmented");
        }

        private static Font font(String key) {
            Font font = UIManager.getFont(key);
            // a plain font: the button's own updateUI must not take it back
            return font != null ? font.deriveFont(font.getStyle()) : null;
        }

        void updateFont() {
            Font font = font(isSelected() ? "SegmentedControl.selectedFont" : "SegmentedControl.font");
            if (font != null) {
                setFont(font);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            if (isPreferredSizeSet() || getText() == null) {
                return size;
            }
            int widest = 0;
            for (String key : new String[] {"SegmentedControl.font", "SegmentedControl.selectedFont"}) {
                Font font = UIManager.getFont(key);
                if (font != null) {
                    widest = Math.max(widest, getFontMetrics(font).stringWidth(getText()));
                }
            }
            int now = getFontMetrics(getFont()).stringWidth(getText());
            size.width += Math.max(0, widest - now);
            return size;
        }
    }

    /** The segments in a row, as tall as the tallest, {@code 2} pixels apart. */
    private static final class RowLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int gap = UIScale.scale(2);
            int width = 0;
            int height = 0;
            for (Component c : parent.getComponents()) {
                Dimension d = c.getPreferredSize();
                width += d.width;
                height = Math.max(height, d.height);
            }
            width += gap * Math.max(0, parent.getComponentCount() - 1);
            return new Dimension(in.left + width + in.right, in.top + height + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int gap = UIScale.scale(2);
            int x = in.left;
            int height = parent.getHeight() - in.top - in.bottom;
            for (Component c : parent.getComponents()) {
                int width = c.getPreferredSize().width;
                c.setBounds(x, in.top, width, height);
                x += width + gap;
            }
        }
    }
}
