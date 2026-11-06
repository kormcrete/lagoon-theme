package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The modern Lagoon's components and style classes on the ground colour, for looking at the theme. Run it as a
 * program for a window; with a file name as the argument it paints the gallery into that PNG instead, no screen
 * needed (run it with {@code -Djava.awt.headless=true}).
 */
public class LagoonModernGallery {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            FlatLagoonModernLaf.setup();
            SwingUtilities.invokeAndWait(() -> {
                try {
                    ImageIO.write(render(gallery()), "png", new File(args[0]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        }
        FlatLagoonModernLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lagoon Modern gallery");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new JScrollPane(gallery()));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /** Lays {@code c} out at its preferred size and paints it into an image, off screen. */
    static BufferedImage render(JComponent c) {
        c.setSize(c.getPreferredSize());
        layOut(c);
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        RepaintManager manager = RepaintManager.currentManager(c);
        boolean buffered = manager.isDoubleBufferingEnabled();
        manager.setDoubleBufferingEnabled(false);
        try {
            c.paint(g);
        } finally {
            manager.setDoubleBufferingEnabled(buffered);
            g.dispose();
        }
        return image;
    }

    static void layOut(Component c) {
        if (c instanceof Container) {
            Container container = (Container) c;
            container.doLayout();
            for (Component child : container.getComponents()) {
                layOut(child);
            }
        }
    }

    static JComponent gallery() {
        int gutter = UIManager.getInt("Lagoon.metrics.gutter");
        int gap = UIManager.getInt("Lagoon.metrics.gap");
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(gutter, gutter, gutter, gutter));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, gap, gap);

        // header: a title, a muted line, the filter and the search
        JPanel header = column(0);
        header.add(styled(new JLabel("Orders"), "h1"));
        header.add(styled(new JLabel("12 open orders · updated just now"), "muted"));
        c.gridwidth = 2;
        root.add(header, c);
        c.gridy++;

        JPanel tools = row(gap);
        tools.add(new SegmentedControl("All 12", "Placed 4", "Shipped 6", "Returned 2"));
        JTextField search = new JTextField(18);
        search.putClientProperty(FlatClientProperties.STYLE_CLASS, "search");
        search.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search orders");
        tools.add(search);
        root.add(tools, c);
        c.gridy++;

        JPanel buttons = row(8);
        buttons.add(styled(new JButton("New order"), "primary"));
        buttons.add(styled(new JButton("Export"), "tonal"));
        buttons.add(new JButton("Print"));
        buttons.add(styled(new JButton("Delete"), "danger"));
        JButton disabled = new JButton("Archive");
        disabled.setEnabled(false);
        buttons.add(disabled);
        root.add(buttons, c);
        c.gridy++;
        c.gridwidth = 1;

        // left: an order on a card with a shadow
        LagoonCard order = new LagoonCard(new BorderLayout(0, gap));
        JPanel who = row(12);
        who.add(new AvatarTile("AS", "Ada Shop Ltd"));
        JPanel name = column(2);
        name.add(styled(new JLabel("Ada Shop Ltd"), "h3"));
        name.add(styled(new JLabel("ORD-1637 · 4 items · EUR 77.50"), "small muted"));
        who.add(name);
        order.add(who, BorderLayout.NORTH);
        JPanel lines = new JPanel(new GridBagLayout());
        lines.setOpaque(false);
        GridBagConstraints l = new GridBagConstraints();
        l.gridy = 0;
        l.insets = new Insets(4, 0, 4, 12);
        l.anchor = GridBagConstraints.WEST;
        String[][] items = {{"A4", "A4 paper", "3"}, {"GP", "Gel pen", "10"}, {"NB", "Notebook", "5"}, {"CB", "Coffee beans", "2"}};
        for (String[] item : items) {
            l.gridx = 0;
            l.weightx = 0;
            AvatarTile tile = new AvatarTile(item[0], item[1]);
            tile.setTileSize(32);
            lines.add(tile, l);
            l.gridx = 1;
            l.weightx = 1;
            lines.add(new JLabel(item[1]), l);
            l.gridx = 2;
            l.weightx = 0;
            l.insets.right = 0;
            Stepper stepper = new Stepper(Integer.parseInt(item[2]), 0, 99);
            stepper.setFilled(l.gridy == 1);
            lines.add(stepper, l);
            l.insets.right = 12;
            l.gridy++;
        }
        order.add(lines, BorderLayout.CENTER);
        JPanel pills = row(6);
        pills.add(new StatusPill("Placed", LagoonTone.INFO));
        pills.add(new StatusPill("Paid", LagoonTone.SUCCESS));
        pills.add(new StatusPill("Awaiting stock", LagoonTone.ATTENTION));
        pills.add(new StatusPill("Cancelled", LagoonTone.DANGER));
        pills.add(new StatusPill("Draft", LagoonTone.NEUTRAL));
        order.add(pills, BorderLayout.SOUTH);
        c.gridx = 0;
        c.weighty = 0;
        root.add(order, c);

        // right: a form on a card style class (a hairline, no shadow)
        JPanel form = column(6);
        form.putClientProperty(FlatClientProperties.STYLE_CLASS, "card");
        form.add(styled(new JLabel("DELIVERY"), "caption muted"));
        form.add(new JTextField("Ada Shop Ltd, 12 Harbour Road", 20));
        JComboBox<String> speed = new JComboBox<>(new String[] {"Standard, 2-3 days", "Express, next day"});
        form.add(speed);
        JPanel qty = row(8);
        qty.add(new JSpinner(new SpinnerNumberModel(2, 1, 20, 1)));
        qty.add(new JCheckBox("Gift wrap", true));
        qty.add(new JRadioButton("Invoice", true));
        form.add(qty);
        JTextField error = new JTextField("12 Harbour Rd", 20);
        error.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        form.add(error);
        form.add(styled(new JLabel("The post code is missing."), "small"));
        ((JLabel) form.getComponent(form.getComponentCount() - 1)).setForeground(UIManager.getColor("Lagoon.color.danger"));
        c.gridx = 1;
        root.add(form, c);
        c.gridy++;

        // text styles and tiles
        JPanel type = column(4);
        type.add(styled(new JLabel("Title, h1"), "h1"));
        type.add(styled(new JLabel("Headline, h2"), "h2"));
        type.add(styled(new JLabel("Heading, h3"), "h3"));
        type.add(new JLabel("Body text, the platform's size"));
        type.add(styled(new JLabel("Small text, small"), "small"));
        type.add(styled(new JLabel("CAPTION, caption muted"), "caption muted"));
        JPanel tiles = row(8);
        for (String n : new String[] {"Ada", "Ben", "Cleo", "Dan", "Eve", "Finn"}) {
            tiles.add(new AvatarTile(n.substring(0, 1), n));
        }
        tiles.add(new AvatarTile(UIManager.getIcon("Tree.leafIcon"), LagoonTone.SUCCESS));
        type.add(tiles);
        c.gridx = 0;
        root.add(type, c);

        // a table on a card
        LagoonCard tableCard = new LagoonCard();
        tableCard.setPadding(new Insets(4, 4, 4, 4));
        DefaultTableModel model = new DefaultTableModel(new Object[][] {
                {"ORD-1637", "Ada Shop Ltd", "EUR 77.50"},
                {"ORD-1636", "Harbour Books", "EUR 12.90"},
                {"ORD-1635", "Ben's Bakery", "EUR 240.00"}}, new Object[] {"Order", "Customer", "Total"});
        JTable table = new JTable(model);
        table.setRowSelectionInterval(0, 0);
        JScrollPane scroll = new JScrollPane(table);
        // the table puts its header there once it is in a window; off screen it is never told
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(360,
                table.getPreferredSize().height + table.getTableHeader().getPreferredSize().height));
        tableCard.add(scroll);
        c.gridx = 1;
        root.add(tableCard, c);
        c.gridy++;

        // a scrim over a small window
        JRootPane window = new JRootPane();
        JPanel behind = column(8);
        behind.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
        behind.add(styled(new JLabel("Orders"), "h2"));
        behind.add(new JLabel("ORD-1637  Ada Shop Ltd"));
        behind.add(new JLabel("ORD-1636  Harbour Books"));
        window.setContentPane(behind);
        window.setPreferredSize(new Dimension(520, 200));
        LagoonCard dialog = new LagoonCard(new BorderLayout(0, gap));
        dialog.add(styled(new JLabel("Cancel order ORD-1637?"), "h3"), BorderLayout.NORTH);
        JPanel answer = row(8);
        answer.add(new JButton("Keep it"));
        answer.add(styled(new JButton("Cancel order"), "danger"));
        dialog.add(answer, BorderLayout.SOUTH);
        c.gridx = 0;
        c.gridwidth = 2;
        root.add(caption("Scrim, over a window"), c);
        c.gridy++;
        root.add(window, c);
        window.setSize(window.getPreferredSize());
        window.doLayout();
        Scrim.show(window, dialog);
        return root;
    }

    private static <T extends JComponent> T styled(T c, String styleClass) {
        c.putClientProperty(FlatClientProperties.STYLE_CLASS, styleClass);
        return c;
    }

    private static JLabel caption(String text) {
        return styled(new JLabel(text.toUpperCase()), "caption muted");
    }

    private static JPanel row(int gap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        // FlowLayout puts the gap before the first component too
        p.setBorder(BorderFactory.createEmptyBorder(0, -gap, 0, 0));
        return p;
    }

    private static JPanel column(int gap) {
        JPanel p = new JPanel(new Stack(gap));
        p.setOpaque(false);
        return p;
    }

    /** Children one under the other, each at its preferred height and the full width. */
    private static final class Stack implements LayoutManager {
        private final int gap;

        Stack(int gap) {
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int width = 0;
            int height = 0;
            for (Component child : parent.getComponents()) {
                Dimension d = child.getPreferredSize();
                width = Math.max(width, d.width);
                height += d.height;
            }
            height += gap * Math.max(0, parent.getComponentCount() - 1);
            return new Dimension(in.left + width + in.right, in.top + height + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int y = in.top;
            int width = parent.getWidth() - in.left - in.right;
            for (Component child : parent.getComponents()) {
                int height = child.getPreferredSize().height;
                child.setBounds(in.left, y, width, height);
                y += height + gap;
            }
        }
    }
}
