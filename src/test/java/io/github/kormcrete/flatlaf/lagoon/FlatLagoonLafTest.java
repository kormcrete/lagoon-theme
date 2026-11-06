package io.github.kormcrete.flatlaf.lagoon;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Headless: installing the look-and-feel needs no display. */
class FlatLagoonLafTest {

    /** An application's custom defaults that turn every switch on and keep the dense sizes (test resources). */
    private static final String DENSE = "io.github.kormcrete.flatlaf.lagoon.dense";

    /** Installs the theme with {@link #DENSE} registered, as a dense application would. */
    private static void dense() {
        FlatLaf.registerCustomDefaultsSource(DENSE);
        FlatLagoonLaf.setup();
    }

    @AfterEach
    void resetLookAndFeel() throws Exception {
        FlatLaf.unregisterCustomDefaultsSource(DENSE);
        UIManager.put("defaultFont", null);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
    }

    @Test
    void installsAsTheCurrentLookAndFeel() {
        assertTrue(FlatLagoonLaf.setup());
        assertEquals(FlatLagoonLaf.class, UIManager.getLookAndFeel().getClass());
        assertEquals("Lagoon", UIManager.getLookAndFeel().getName());
    }

    @Test
    void loadsItsPropertiesFile() {
        FlatLagoonLaf.setup();
        // A handful of keys that only the properties file sets; FlatLightLaf's own values differ.
        assertEquals(new Color(0x5F6265), UIManager.getColor("Component.borderColor"));
        assertEquals(2, UIManager.getInt("Component.arc"));
        assertEquals("card", UIManager.getString("TabbedPane.tabType"));
        // Square popups, so the theme's own frame is painted, not the platform's rounded window.
        assertEquals(0, UIManager.getInt("PopupMenu.borderCornerRadius"));
        assertEquals(0, UIManager.getInt("ComboBox.borderCornerRadius"));
        // A toolbar button's hover is the menu bar's light one, not a darkened button.
        assertEquals(UIManager.getColor("MenuBar.hoverBackground"), UIManager.getColor("Button.toolbar.hoverBackground"));
        assertEquals(UIManager.getColor("MenuBar.hoverBackground"), UIManager.getColor("ToggleButton.toolbar.hoverBackground"));
        // ...and a platform-drawn popup border takes the theme's frame at one pixel (macOS defaults to none). FlatLaf
        // reads the width per popup kind only on macOS; Windows 11 draws its own.
        assertEquals(1f, ((Number) UIManager.get("Popup.roundedBorderWidth")).floatValue());
        if (SystemInfo.isMacOS) {
            assertEquals(1f, ((Number) UIManager.get("PopupMenu.roundedBorderWidth")).floatValue());
        }
        assertEquals(16, UIManager.getInt("ScrollBar.width"));
        assertEquals(new Color(0xBCBDBE), UIManager.getColor("Table.gridColor"));
    }

    /**
     * On macOS FlatLaf puts every popup with a drop shadow into its own window, which macOS animates as it opens and
     * rims with its shadow next to the theme's frame. Without the shadow a menu inside the window is painted in it.
     */
    @Test
    void onMacOSPopupsHaveNoDropShadowSoMenusStayInTheWindow() {
        FlatLagoonLaf.setup();
        assertEquals(!SystemInfo.isMacOS, UIManager.getBoolean("Popup.dropShadowPainted"));
    }

    @Test
    void registersItsOwnDelegatesAndBorders() {
        FlatLagoonLaf.setup();
        assertEquals(LagoonTabbedPaneUI.class.getName(), UIManager.getString("TabbedPaneUI"));
        // Named by class in the properties file: a misspelt name only logs, so check each one resolved.
        assertEquals(LagoonBorders.Text.class, UIManager.getBorder("TextField.border").getClass());
        assertEquals(LagoonBorders.Text.class, UIManager.getBorder("PasswordField.border").getClass());
        assertEquals(LagoonBorders.Round.class, UIManager.getBorder("ComboBox.border").getClass());
        assertEquals(LagoonBorders.Round.class, UIManager.getBorder("Spinner.border").getClass());
        assertEquals(LagoonBorders.Button.class, UIManager.getBorder("Button.border").getClass());
        assertEquals(LagoonBorders.Button.class, UIManager.getBorder("ToggleButton.border").getClass());
        assertEquals(LagoonBorders.ScrollPane.class, UIManager.getBorder("ScrollPane.border").getClass());
        assertEquals(LagoonComboBoxUI.class.getName(), UIManager.getString("ComboBoxUI"));
        assertEquals(LagoonSpinnerUI.class.getName(), UIManager.getString("SpinnerUI"));
        assertEquals(LagoonButtonUI.class.getName(), UIManager.getString("ButtonUI"));
        assertEquals(LagoonTableHeaderUI.class.getName(), UIManager.getString("TableHeaderUI"));
        Border titled = UIManager.getBorder("TitledBorder.border");
        assertEquals(new Insets(2, 2, 2, 2), titled.getBorderInsets(new JPanel()));
    }

    /** Menus and the toolbar in the regular weight, as in FlatLaf Light. */
    @Test
    void menusAndTheToolbarAreRegularByDefault() {
        FlatLagoonLaf.setup();
        for (String key : new String[]{"MenuBar.font", "Menu.font", "MenuItem.font", "ToolBar.font"}) {
            assertEquals(Font.PLAIN, UIManager.getFont(key).getStyle(), key);
        }
    }

    @Test
    void withTheDialogFontSwitchBasesFontsOnDialog12WithBoldMenus() {
        dense();
        Font label = UIManager.getFont("Label.font");
        assertEquals("Dialog", label.getName());
        assertEquals(Font.PLAIN, label.getStyle());
        assertEquals(Font.BOLD, UIManager.getFont("MenuBar.font").getStyle());
    }

    @Test
    void leavesAnApplicationsOwnBaseFontAlone() {
        UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        dense();
        Font label = UIManager.getFont("Label.font");
        assertEquals(Font.SANS_SERIF, label.getName());
    }

    /**
     * Every FlatLaf theme puts a {@code defaultFont} into its own defaults, which
     * {@code UIManager.get} also reads: that one is the previous theme's, not the application's.
     */
    @Test
    void switchingFromAnotherFlatLafThemeStillBasesFontsOnDialog12() {
        FlatLightLaf.setup();
        dense();
        assertEquals("Dialog", UIManager.getFont("Label.font").getName());
    }

    @Test
    void labelsKeepTheirIconTextGapAfterTheText() {
        dense();
        JLabel label = new JLabel("Username");
        int text = label.getFontMetrics(label.getFont()).stringWidth("Username");
        assertEquals(text + label.getIconTextGap(), label.getPreferredSize().width);
        // HTML text takes the same room as plain text: a centred label must not shift when it turns into a link.
        assertEquals(text + label.getIconTextGap(), new JLabel("<html>Username</html>").getPreferredSize().width);
        // Swing derives the minimum from the preferred size; the gap must not be added twice.
        assertEquals(label.getPreferredSize().width, label.getMinimumSize().width);
        // A list renderer is a label too, but its size is a list item's or a combo box value's.
        javax.swing.DefaultListCellRenderer renderer = new javax.swing.DefaultListCellRenderer();
        renderer.setText("Username");
        assertEquals(text + renderer.getInsets().left + renderer.getInsets().right, renderer.getPreferredSize().width);
        // A table cell keeps the gap, so a column fitted to its widest cell leaves the same room as a label.
        javax.swing.table.DefaultTableCellRenderer cell = new javax.swing.table.DefaultTableCellRenderer();
        cell.setText("Username");
        assertEquals(text + cell.getIconTextGap() + cell.getInsets().left + cell.getInsets().right,
                cell.getPreferredSize().width);
    }

    @Test
    void checkBoxesAndRadioButtonsHaveTwelvePixelIconsWhoseFrameFillsThem() {
        FlatLagoonLaf.setup();
        javax.swing.Icon check = UIManager.getIcon("CheckBox.icon");
        javax.swing.Icon radio = UIManager.getIcon("RadioButton.icon");
        assertEquals(new Dimension(12, 12), new Dimension(check.getIconWidth(), check.getIconHeight()));
        assertEquals(new Dimension(12, 12), new Dimension(radio.getIconWidth(), radio.getIconHeight()));
        JCheckBox box = new JCheckBox("Print", true);
        BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        check.paintIcon(box, g, 0, 0);
        g.dispose();
        int frame = UIManager.getColor("CheckBox.icon.selectedBorderColor").getRGB();
        assertEquals(frame, image.getRGB(0, 6));
        assertEquals(frame, image.getRGB(11, 6));
        assertEquals(frame, image.getRGB(6, 0));
        assertEquals(frame, image.getRGB(6, 11));
    }

    @Test
    void aTablesBooleanCellsShowTheThemesCheckBoxAndGiveTheRendererBackOnASwitch() throws Exception {
        FlatLagoonLaf.setup();
        JTable table = new JTable(new Object[][] {{true}, {false}}, new Object[] {"Done"}) {
            @Override
            public Class<?> getColumnClass(int column) {
                return Boolean.class;
            }
        };
        javax.swing.table.TableCellRenderer renderer = table.getDefaultRenderer(Boolean.class);
        JLabel on = (JLabel) renderer.getTableCellRendererComponent(table, true, false, false, 0, 0);
        assertEquals(12, on.getIcon().getIconWidth());
        int[] checked = paint(on.getIcon(), on);
        JLabel off = (JLabel) renderer.getTableCellRendererComponent(table, false, false, false, 1, 0);
        assertTrue(!java.util.Arrays.equals(checked, paint(off.getIcon(), off)), "a true and a false cell look alike");

        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.updateComponentTreeUI(table);
        assertTrue(table.getDefaultRenderer(Boolean.class) instanceof JCheckBox,
                "the table kept " + table.getDefaultRenderer(Boolean.class));
    }

    private static int[] paint(javax.swing.Icon icon, java.awt.Component c) {
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        icon.paintIcon(c, g, 0, 0);
        g.dispose();
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    @Test
    void aFieldEditingATableCellKeepsTheFieldsFrameAndInsets() {
        FlatLagoonLaf.setup();
        JTable table = new JTable(2, 2);
        JTextField field = new JTextField();
        table.setDefaultEditor(Object.class, new javax.swing.DefaultCellEditor(field));
        table.editCellAt(0, 0);
        assertTrue(table.getEditorComponent() == field, "not editing");
        assertEquals(new JTextField().getInsets(), field.getInsets());
        field.setSize(80, 19);
        BufferedImage image = new BufferedImage(80, 19, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        field.getBorder().paintBorder(field, g, 0, 0, 80, 19);
        g.dispose();
        assertEquals(UIManager.getColor("Component.ringShadowColor").getRGB(), image.getRGB(0, 9));
        assertEquals(UIManager.getColor("Component.borderColor").getRGB(), image.getRGB(1, 9));
    }

    @Test
    void buttonsKeepTheGapAfterTheTextAndAnOddSize() {
        dense();
        JButton save = new JButton("Speichern");
        int text = save.getFontMetrics(save.getFont()).stringWidth("Speichern");
        java.awt.Insets in = save.getInsets();
        assertEquals((text + save.getIconTextGap() + in.left + in.right) | 1, save.getPreferredSize().width);
        assertEquals(1, save.getPreferredSize().height % 2);
        assertEquals(save.getPreferredSize(), new JButton("<html>Speichern</html>").getPreferredSize());
    }

    /**
     * Field, combo, spinner and button heights the layout of a form is built on: the text's height plus the
     * theme's room around it (21, 23, 24 and 25px with a 15px line, as Dialog 12 has on macOS and Linux).
     */
    @Test
    void sizesControlsForADenseForm() {
        dense();
        int text = textHeight(new JTextField());
        assertEquals(text + 6, new JTextField(10).getPreferredSize().height);
        assertEquals(text + 6, new JPasswordField(10).getPreferredSize().height);
        assertEquals(text + 8, new JComboBox<>(new String[]{"x"}).getPreferredSize().height);
        assertEquals(text + 9, new JSpinner().getPreferredSize().height);
        // two columns for 99 (a column is an m wide), then frame, padding and the buttons: 47px for Dialog 12
        JSpinner twoDigits = new JSpinner(new javax.swing.SpinnerNumberModel(12, 0, 99, 1));
        assertEquals(2 * twoDigits.getFontMetrics(twoDigits.getFont()).charWidth('m') + 25,
                twoDigits.getPreferredSize().width);
        assertEquals((text + 10) | 1, new JButton("Save").getPreferredSize().height);
        assertEquals(text + 8, new JCheckBox("x").getPreferredSize().height);
    }

    /** The height of a line of the component's text. */
    private static int textHeight(javax.swing.JComponent c) {
        return c.getFontMetrics(c.getFont()).getHeight();
    }

    /**
     * An editable combo box's text starts where a field's does, so a column of fields and combo boxes reads as
     * one edge; a list value (its renderer brings a one-pixel border of its own) one pixel further in.
     */
    @Test
    void aComboBoxsTextLinesUpWithAFieldsText() {
        dense();
        JComboBox<String> editable = new JComboBox<>(new String[]{"Hello"});
        editable.setEditable(true);
        int field = textInkFromFrame(new JTextField("Hello"));
        assertEquals(field, textInkFromFrame(editable), "editable combo box");
        JComboBox<String> list = new JComboBox<>(new String[]{"Hello"});
        assertEquals(field + 1, textInkFromFrame(list), "list value");
        // frame, padding, the renderer's border and the arrow button, as wide as the text is high: the width a
        // form was laid out for
        assertEquals(list.getFontMetrics(list.getFont()).stringWidth("Hello") + textHeight(list) + 14,
                list.getPreferredSize().width);
    }

    /** A tree row is as tall as its icon or text, flush at the tree's edge, 20px further in per level. */
    @Test
    void treeRowsAreAsTallAsTheirContentAndStepTwentyPixelsALevel() {
        dense();
        javax.swing.JTree tree = settingsLikeTree();
        assertEquals(new Insets(0, 0, 0, 0), tree.getInsets());
        assertEquals(java.util.Arrays.asList(20, 40, 20), rowXs(tree));
        javax.swing.tree.DefaultTreeCellRenderer renderer = (javax.swing.tree.DefaultTreeCellRenderer) tree.getCellRenderer();
        assertEquals(new Insets(0, 0, 0, 0), renderer.getInsets(), "text starts where the row does");
        assertEquals(16, tree.getRowBounds(0).height, "a row with a 16px icon");

        javax.swing.tree.DefaultTreeCellRenderer textOnly = new javax.swing.tree.DefaultTreeCellRenderer();
        textOnly.setLeafIcon(null);
        textOnly.setOpenIcon(null);
        textOnly.setClosedIcon(null);
        javax.swing.JTree text = settingsLikeTree();
        text.setCellRenderer(textOnly);
        text.updateUI(); // the row heights were cached with the icons
        assertEquals(text.getFontMetrics(text.getFont()).getHeight(), text.getRowBounds(0).height, "a row of text");
    }

    /**
     * A tree row of text alone keeps the label gap after its text, as a label does; a row with an icon does not
     * (Swing's tree renderer adds 3px either way).
     */
    @Test
    void aTextOnlyTreeRowKeepsTheLabelGapAfterItsText() {
        dense();
        javax.swing.JTree tree = settingsLikeTree();
        javax.swing.tree.DefaultTreeCellRenderer renderer = new javax.swing.tree.DefaultTreeCellRenderer();
        renderer.getTreeCellRendererComponent(tree, "Parent", false, false, true, 0, false);
        renderer.setIcon(null);
        int text = renderer.getFontMetrics(renderer.getFont()).stringWidth("Parent");
        Insets in = renderer.getInsets();
        assertEquals(text + renderer.getIconTextGap() + 3 + in.left + in.right, renderer.getPreferredSize().width);
        javax.swing.Icon icon = UIManager.getIcon("Tree.leafIcon") != null ? UIManager.getIcon("Tree.leafIcon")
                : new javax.swing.ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        renderer.setIcon(icon);
        assertEquals(icon.getIconWidth() + renderer.getIconTextGap() + text + 3 + in.left + in.right,
                renderer.getPreferredSize().width, "with an icon");
    }

    /** A tree may ask for no lines with the standard {@code JTree.lineStyle} client property. */
    @Test
    void aTreeAskingForNoLinesPaintsNone() {
        dense();
        int hash = UIManager.getColor("Tree.hash").getRGB() & 0xFFFFFF;
        assertTrue(pixelsOf(hash, settingsLikeTree()) > 0, "lines by default");
        javax.swing.JTree plain = settingsLikeTree();
        plain.putClientProperty("JTree.lineStyle", "None");
        assertEquals(0, pixelsOf(hash, plain));
    }

    private static javax.swing.JTree settingsLikeTree() {
        javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode("root");
        javax.swing.tree.DefaultMutableTreeNode parent = new javax.swing.tree.DefaultMutableTreeNode("Parent");
        parent.add(new javax.swing.tree.DefaultMutableTreeNode("Child"));
        root.add(parent);
        root.add(new javax.swing.tree.DefaultMutableTreeNode("Leaf"));
        javax.swing.JTree tree = new javax.swing.JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.expandRow(0);
        tree.setSize(tree.getPreferredSize());
        return tree;
    }

    private static java.util.List<Integer> rowXs(javax.swing.JTree tree) {
        java.util.List<Integer> xs = new java.util.ArrayList<>();
        for (int row = 0; row < tree.getRowCount(); row++) {
            xs.add(tree.getRowBounds(row).x);
        }
        return xs;
    }

    private static int pixelsOf(int rgb, javax.swing.JComponent c) {
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
        int n = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) == rgb) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Dense menus: 19px items, 21px with an icon or a check mark (with a 15px line), 3px separators. Text starts one column in
     * every item of a popup: near the edge when no item has an icon, after the icon column when one does.
     */
    @Test
    void menusAreDenseAndAlignTheirText() {
        dense();
        javax.swing.Icon icon = new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                g.setColor(Color.BLACK);
                g.fillRect(x, y, 16, 16);
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
        javax.swing.JPopupMenu plain = new javax.swing.JPopupMenu();
        javax.swing.JMenu sub = new javax.swing.JMenu("Kxyz");
        sub.add(new javax.swing.JMenuItem("x"));
        plain.add(sub);
        plain.add(new javax.swing.JMenuItem("Kxyz"));
        plain.add(new javax.swing.JCheckBoxMenuItem("Kxyz", true));
        plain.addSeparator();
        plain.add(new javax.swing.JRadioButtonMenuItem("Kxyz", true));
        int[][] p = menuItemInk(plain);
        // as tall as the text, or as a check mark or an icon where that is taller
        int text = textHeight(sub);
        int check = Math.max(text, UIManager.getIcon("CheckBoxMenuItem.checkIcon").getIconHeight());
        int radio = Math.max(text, UIManager.getIcon("RadioButtonMenuItem.checkIcon").getIconHeight());
        assertEquals(java.util.Arrays.asList(text + 6, text + 4, check + 6, 3, radio + 6), heights(plain));
        // where the text starts; its ink starts a glyph's side bearing later, which differs from font to font
        int k = inkBearing(sub, "Kxyz");
        assertEquals(TEXT_START, p[0][0] - k, "submenu text");
        assertEquals(TEXT_START, p[1][0] - k, "item text");
        assertEquals(TEXT_START, p[2][p[2].length - 1] - k, "check box item text");
        assertEquals(TEXT_START, p[4][p[4].length - 1] - k, "radio button item text");
        assertTrue(separatorLineShows(plain, 3), "the separator's line lies inside its 3px");

        javax.swing.JPopupMenu withIcons = new javax.swing.JPopupMenu();
        withIcons.add(new javax.swing.JMenuItem("Kxyz", icon));
        withIcons.add(new javax.swing.JMenuItem("Kxyz"));
        int[][] w = menuItemInk(withIcons);
        assertEquals(java.util.Arrays.asList(Math.max(text, 16) + 5, text + 4), heights(withIcons));
        assertEquals(23, w[0][0], "icon");
        assertEquals(TEXT_START + 20, w[0][1] - k, "text after the icon");
        assertEquals(TEXT_START + 20, w[1][0] - k, "text of an item without one");
    }

    /** Where a popup without icons starts its items' text. */
    private static final int TEXT_START = 23;

    /** Columns from where the component's font starts the text to its first column of ink, as a label paints it. */
    private static int inkBearing(javax.swing.JComponent c, String text) {
        JLabel label = new JLabel(text);
        label.setFont(c.getFont());
        label.setSize(label.getPreferredSize());
        BufferedImage image = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        label.paint(g);
        g.dispose();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (luminance(image.getRGB(x, y)) < 120) {
                    return x - label.getInsets().left;
                }
            }
        }
        throw new AssertionError("no ink for " + text);
    }

    private static boolean separatorLineShows(javax.swing.JPopupMenu popup, int index) {
        java.awt.Component sep = popup.getComponent(index);
        BufferedImage image = new BufferedImage(popup.getWidth(), popup.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        popup.paint(g);
        g.dispose();
        int line = UIManager.getColor("Separator.foreground").getRGB() & 0xFFFFFF;
        for (int y = sep.getY(); y < sep.getY() + sep.getHeight(); y++) {
            if ((image.getRGB(sep.getX() + sep.getWidth() / 2, y) & 0xFFFFFF) == line) {
                return true;
            }
        }
        return false;
    }

    private static java.util.List<Integer> heights(javax.swing.JPopupMenu popup) {
        java.util.List<Integer> h = new java.util.ArrayList<>();
        for (java.awt.Component c : popup.getComponents()) {
            h.add(c.getHeight());
        }
        return h;
    }

    /** Per item: the popup columns where runs of ink (gaps over 3px apart) start. */
    private static int[][] menuItemInk(javax.swing.JPopupMenu popup) {
        popup.setSize(popup.getPreferredSize());
        layOut(popup);
        BufferedImage image = new BufferedImage(popup.getWidth(), popup.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        popup.paint(g);
        g.dispose();
        int[][] runs = new int[popup.getComponentCount()][];
        for (int i = 0; i < runs.length; i++) {
            java.awt.Component c = popup.getComponent(i);
            java.util.List<Integer> starts = new java.util.ArrayList<>();
            int last = -10;
            for (int x = c.getX(); x < c.getX() + c.getWidth(); x++) {
                for (int y = c.getY() + 1; y < c.getY() + c.getHeight() - 1; y++) {
                    if (luminance(image.getRGB(x, y)) < 120) {
                        if (x - last > 3) {
                            starts.add(x);
                        }
                        last = x;
                        break;
                    }
                }
            }
            runs[i] = starts.stream().mapToInt(Integer::intValue).toArray();
        }
        return runs;
    }

    /** A message's buttons sit centred under it, Yes first, each as wide as the widest label needs. */
    @Test
    void anOptionPaneCentresItsButtonsYesFirst() {
        dense();
        javax.swing.JOptionPane pane = new javax.swing.JOptionPane("Save the changes?",
                javax.swing.JOptionPane.QUESTION_MESSAGE, javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);
        pane.setSize(pane.getPreferredSize());
        layOut(pane);
        java.util.List<JButton> buttons = new java.util.ArrayList<>();
        collectButtons(pane, buttons);
        buttons.sort(java.util.Comparator.comparingInt(b -> javax.swing.SwingUtilities.convertPoint(b, 0, 0, pane).x));
        assertEquals(java.util.Arrays.asList(UIManager.getString("OptionPane.yesButtonText"),
                UIManager.getString("OptionPane.noButtonText"), UIManager.getString("OptionPane.cancelButtonText")),
                java.util.Arrays.asList(buttons.get(0).getText(), buttons.get(1).getText(), buttons.get(2).getText()));
        int left = javax.swing.SwingUtilities.convertPoint(buttons.get(0), 0, 0, pane).x;
        JButton last = buttons.get(2);
        int right = javax.swing.SwingUtilities.convertPoint(last, last.getWidth(), 0, pane).x;
        assertEquals(pane.getWidth() - right, left, 1, "centred");
        assertEquals(new Insets(10, 10, 12, 10), pane.getInsets());
        JButton cancel = buttons.get(2);
        int text = cancel.getFontMetrics(cancel.getFont()).stringWidth(cancel.getText());
        assertTrue(cancel.getWidth() < text + 30, "no minimum width: " + cancel.getWidth() + " for " + text);
        assertEquals((textHeight(cancel) + 8) | 1, cancel.getHeight(), "the narrow message-button margin");
    }

    private static void layOut(java.awt.Container c) {
        c.doLayout();
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof java.awt.Container) {
                layOut((java.awt.Container) child);
            }
        }
    }

    private static void collectButtons(java.awt.Container c, java.util.List<JButton> into) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof JButton) {
                into.add((JButton) child);
            } else if (child instanceof java.awt.Container) {
                collectButtons((java.awt.Container) child, into);
            }
        }
    }

    /** Columns from the frame's left line to the first column of text ink. */
    private static int textInkFromFrame(javax.swing.JComponent c) {
        JPanel holder = new JPanel(null);
        holder.add(c);
        c.setSize(160, c.getPreferredSize().height);
        c.doLayout();
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
        int middle = c.getHeight() / 2, frame = -1;
        for (int x = 0; x < 20 && frame < 0; x++) {
            if (luminance(image.getRGB(x, middle)) < 150) {
                frame = x;
            }
        }
        for (int x = frame + 1; x < 60; x++) {
            for (int y = 3; y < c.getHeight() - 3; y++) {
                if (luminance(image.getRGB(x, y)) < 110) {
                    return x - frame;
                }
            }
        }
        throw new AssertionError("no text in " + c);
    }

    private static int luminance(int rgb) {
        return (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
    }

    @Test
    void keepsOneSelectionColourWithAndWithoutFocus() {
        dense();
        assertEquals(UIManager.getColor("Table.selectionBackground"),
                UIManager.getColor("Table.selectionInactiveBackground"));
        assertEquals(UIManager.getColor("List.selectionBackground"),
                UIManager.getColor("List.selectionInactiveBackground"));
    }

    /** The frame round a tab's content is one line, with room inside it on the three sides away from the tabs. */
    @Test
    void tabContentSitsInsideItsFrameWithRoomToSpare() {
        dense();
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        JPanel content = new JPanel();
        tabs.addTab("Orders", content);
        tabs.setSize(300, 200);
        tabs.doLayout();
        assertEquals(3, content.getX());
        // One line and one pixel of room below the tab.
        assertEquals(tabs.getBoundsAt(0).y + tabs.getBoundsAt(0).height + 2, content.getY());
        assertEquals(300 - 3, content.getX() + content.getWidth());
        assertEquals(200 - 3, content.getY() + content.getHeight());
        assertEquals(1, UIManager.getInt("TabbedPane.contentSeparatorHeight"));
    }

    /**
     * By default the unselected tabs start two pixels lower than the selected one, their text centred in what is
     * left; with no raise they start on its line.
     */
    @Test
    void theSelectedTabStandsAboveTheOthersByTheRaise() {
        FlatLagoonLaf.setup();
        int unselectedFrame = UIManager.getColor("TabbedPane.unselectedBorderColor").getRGB();
        assertEquals(2, UIManager.getInt("TabbedPane.selectedTabRaise"));
        int[] edge = tabTopEdge(2);
        assertTrue(edge[0] != unselectedFrame && edge[1] != unselectedFrame, "the two rows above it are not the tab's");
        assertEquals(unselectedFrame, edge[2]);
        // Each title against itself without the raise: where ink starts depends on the glyphs and the font.
        int[] inkTopWithRaise = lastTitleInkTop.clone();
        UIManager.put("TabbedPane.selectedTabRaise", 0);
        try {
            assertEquals(unselectedFrame, tabTopEdge(0)[0], "no raise: the unselected tab starts on the selected one's line");
            assertEquals(lastTitleInkTop[0], inkTopWithRaise[0], "the selected title stays");
            assertEquals(lastTitleInkTop[1] + 1, inkTopWithRaise[1], "the unselected title one row lower");
        } finally {
            UIManager.put("TabbedPane.selectedTabRaise", null);
        }
    }

    /** The first row of title ink in the selected and the unselected tab, from the last {@link #tabTopEdge}. */
    private static final int[] lastTitleInkTop = new int[2];

    /** The first row inside a tab, below its frame, with dark text pixels that do not span the tab. */
    private static int inkTop(BufferedImage image, java.awt.Rectangle tab) {
        for (int y = tab.y + 3; y < tab.y + tab.height; y++) {
            int dark = 0;
            for (int x = tab.x + 3; x < tab.x + tab.width - 3; x++) {
                Color c = new Color(image.getRGB(x, y), true);
                if (c.getAlpha() > 0 && c.getRed() + c.getGreen() + c.getBlue() < 3 * 130) {
                    dark++;
                }
            }
            if (dark > 0 && dark < (tab.width - 6) * 6 / 10) {
                return y;
            }
        }
        return -1;
    }

    /**
     * The first three pixels down the middle of the second (unselected) tab of a tab strip, from its top, and checks
     * its label shift: half the raise.
     */
    private static int[] tabTopEdge(int raise) {
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab("Orders", new JPanel());
        tabs.addTab("Customers", new JPanel());
        tabs.setSize(300, 200);
        tabs.doLayout();
        assertEquals(raise / 2, ((LagoonTabbedPaneUI) tabs.getUI()).getTabLabelShiftY(javax.swing.SwingConstants.TOP, 1, false));
        assertEquals(0, ((LagoonTabbedPaneUI) tabs.getUI()).getTabLabelShiftY(javax.swing.SwingConstants.TOP, 0, true));
        BufferedImage image = new BufferedImage(tabs.getWidth(), tabs.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        tabs.paint(g);
        g.dispose();
        for (int i = 0; i < 2; i++) {
            lastTitleInkTop[i] = inkTop(image, tabs.getBoundsAt(i));
        }
        java.awt.Rectangle tab = tabs.getBoundsAt(1);
        int[] edge = new int[3];
        for (int i = 0; i < edge.length; i++) {
            edge[i] = image.getRGB(tab.x + tab.width / 2, tab.y + i);
        }
        return edge;
    }

    /** A menu bar's open menu is framed on three sides (its popup's frame closes it below); a closed one is not. */
    @Test
    void aMenuBarsOpenMenuIsFramed() {
        FlatLagoonLaf.setup();
        int frame = UIManager.getColor("MenuBar.selectionBorderColor").getRGB();
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        javax.swing.JMenu menu = new javax.swing.JMenu("Options");
        bar.add(menu);
        bar.setSize(bar.getPreferredSize());
        bar.doLayout();
        assertTrue(menuEdge(menu) != frame, "closed");
        menu.setSelected(true);
        assertEquals(frame, menuEdge(menu), "open");
        assertTrue(menuBottom(menu) != frame, "no line below, where the popup's frame is");
    }

    /** The pixel at the middle of a menu's bottom edge. */
    private static int menuBottom(javax.swing.JMenu menu) {
        BufferedImage image = new BufferedImage(menu.getWidth(), menu.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        menu.paint(g);
        g.dispose();
        return image.getRGB(menu.getWidth() / 2, menu.getHeight() - 1);
    }

    /** The pixel at the middle of a menu's left edge. */
    private static int menuEdge(javax.swing.JMenu menu) {
        BufferedImage image = new BufferedImage(menu.getWidth(), menu.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        menu.paint(g);
        g.dispose();
        return image.getRGB(0, menu.getHeight() / 2);
    }

    /**
     * A selected row keeps the colour its renderer gives it, made more vivid; a plain row gets the selection colour.
     */
    @Test
    void aSelectedRowKeepsItsOwnColourMadeMoreVivid() {
        FlatLagoonLaf.setup();
        Color amber = new Color(0xFFE9A8);
        JTable table = new JTable(3, 2);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row == 0 ? amber : t.getBackground());
                }
                return this;
            }
        });
        table.setSize(200, table.getRowHeight() * 3);
        table.selectAll();
        BufferedImage image = new BufferedImage(table.getWidth(), table.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        table.paint(g);
        g.dispose();
        int middle = table.getRowHeight() / 2;
        assertEquals(LagoonTableUI.amplified(amber).getRGB(), image.getRGB(50, middle), "the coloured row");
        assertEquals(table.getSelectionBackground().getRGB(), image.getRGB(50, table.getRowHeight() + middle),
                "a plain row");

        // A table can opt out: then the coloured row is selected like any other.
        table.putClientProperty(LagoonTableUI.SELECTION_FOLLOWS_CELL_COLOR, false);
        g = image.createGraphics();
        table.paint(g);
        g.dispose();
        assertEquals(table.getSelectionBackground().getRGB(), image.getRGB(50, middle), "opted out");
    }

    @Test
    void aToolbarSeparatorKeepsItsLineInAFlowLayoutAndNoSizeOutlivesASwitch() {
        FlatLagoonLaf.setup();
        JToolBar bar = new JToolBar();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        bar.addSeparator();
        assertEquals(new Dimension(16, 12), bar.getComponent(0).getPreferredSize());

        FlatLightLaf.setup();
        SwingUtilities.updateComponentTreeUI(bar);
        assertEquals(0, bar.getComponent(0).getPreferredSize().height);
    }

    @Test
    void contentIsFramedInATabsLineAndTextInTheFieldsFrame() {
        FlatLagoonLaf.setup();
        int tab = UIManager.getColor("TabbedPane.contentAreaColor").getRGB();
        int field = UIManager.getColor("Component.borderColor").getRGB();
        int[] table = frameLeftEdge(new JScrollPane(new JTable(3, 3)));
        assertEquals(0, table[0] >>> 24, "no ring around content");
        assertEquals(tab, table[1]);
        assertEquals(field, frameLeftEdge(new JScrollPane(new javax.swing.JTextArea(3, 10)))[1]);
    }

    @Test
    void aTableFillingATabKeepsItsOwnFrame() {
        FlatLagoonLaf.setup();
        JScrollPane scrollPane = new JScrollPane(new JTable(3, 3));
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab("One", scrollPane);
        tabs.setSize(300, 200);
        tabs.doLayout();
        assertEquals(UIManager.getColor("TabbedPane.contentAreaColor").getRGB(), frameLeftEdge(scrollPane)[1]);
    }

    /** The first pixels of the middle row of a scroll pane's border, painted on its own. */
    private static int[] frameLeftEdge(JScrollPane scrollPane) {
        if (scrollPane.getWidth() == 0) {
            scrollPane.setSize(200, 100);
        }
        BufferedImage image = new BufferedImage(scrollPane.getWidth(), scrollPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        scrollPane.getBorder().paintBorder(scrollPane, g, 0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        int[] edge = new int[3];
        for (int x = 0; x < edge.length; x++) {
            edge[x] = image.getRGB(x, image.getHeight() / 2);
        }
        return edge;
    }

    @Test
    void aTablesScrollPaneKeepsTheThemesFrameSoFocusPaintsNoRing() {
        FlatLagoonLaf.setup();
        JTable table = new JTable(3, 3) {
            {
                // What JTable does on addNotify, which a headless test never reaches.
                new JScrollPane(this);
                configureEnclosingScrollPane();
            }
        };
        assertEquals(LagoonBorders.ScrollPane.class, ((JScrollPane) table.getParent().getParent()).getBorder().getClass());
    }

    @Test
    void aLongThumbCarriesThreeRidgesAcrossItsMiddleAndAShortOneNone() {
        FlatLagoonLaf.setup();
        int shadow = UIManager.getColor("ScrollBar.gripShadowColor").getRGB();
        // A thumb of 30 px along a 15 px track breadth: its centre column crosses every ridge once.
        assertEquals(3, runsOf(shadow, thumbCentreColumn(new JScrollBar(JScrollBar.VERTICAL, 0, 40, 0, 100))));
        // Too short to hold them with room to spare.
        assertEquals(0, runsOf(shadow, thumbCentreColumn(new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 100))));
    }

    /**
     * The thumb is light at rest; the mouse anywhere on the bar deepens its fill, frame and ridges together. Measured
     * flat, where the fill is the colour itself (the shaded fills: {@link LagoonShadeTest}).
     */
    @Test
    void aThumbDeepensWithItsFrameAndRidgesWhileTheMouseIsOnTheBar() {
        FlatLagoonLaf.setup();
        UIManager.put("Lagoon.shade", "flat");
        try {
            aFlatThumbDeepensWhileTheMouseIsOnTheBar();
        } finally {
            UIManager.put("Lagoon.shade", null);
        }
    }

    private static void aFlatThumbDeepensWhileTheMouseIsOnTheBar() {
        JScrollBar bar = new JScrollBar(JScrollBar.VERTICAL, 0, 40, 0, 100);
        int[] resting = thumbCentreColumn(bar);
        assertTrue(runsOf(UIManager.getColor("ScrollBar.thumbBorderColor").getRGB(), resting) > 0, "resting frame");
        assertEquals(3, runsOf(UIManager.getColor("ScrollBar.gripShadowColor").getRGB(), resting));
        assertEquals(0, runsOf(UIManager.getColor("ScrollBar.hoverThumbColor").getRGB(), resting));

        // On the track below the thumb, not on the thumb itself.
        bar.dispatchEvent(new java.awt.event.MouseEvent(bar, java.awt.event.MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(), 0, 8, 80, 0, false));
        int[] hovered = thumbCentreColumn(bar);
        assertTrue(runsOf(UIManager.getColor("ScrollBar.hoverThumbColor").getRGB(), hovered) > 0, "hover fill");
        assertTrue(runsOf(UIManager.getColor("ScrollBar.hoverThumbBorderColor").getRGB(), hovered) > 0, "hover frame");
        assertEquals(3, runsOf(UIManager.getColor("ScrollBar.hoverGripShadowColor").getRGB(), hovered));
    }

    private static int[] thumbCentreColumn(JScrollBar bar) {
        bar.setSize(16, 100);
        bar.doLayout();
        BufferedImage image = new BufferedImage(bar.getWidth(), bar.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        bar.paint(g);
        g.dispose();
        int[] column = new int[image.getHeight()];
        for (int y = 0; y < column.length; y++) {
            column[y] = image.getRGB(1 + 15 / 2, y);
        }
        return column;
    }

    private static int runsOf(int rgb, int[] pixels) {
        int runs = 0;
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == rgb && (i == 0 || pixels[i - 1] != rgb)) {
                runs++;
            }
        }
        return runs;
    }

    @Test
    void aTablesVerticalScrollBarStartsBelowItsHeaderWithSquareButtonsInsideItsLine() {
        FlatLagoonLaf.setup();
        JTable table = new JTable(200, 3);
        JScrollPane scrollPane = new JScrollPane(table);
        // What JTable does on addNotify, which a headless test never reaches.
        scrollPane.setColumnHeaderView(table.getTableHeader());
        scrollPane.setSize(400, 300);
        scrollPane.doLayout();
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.doLayout();
        assertEquals(scrollPane.getColumnHeader().getY() + scrollPane.getColumnHeader().getHeight(), bar.getY());
        assertEquals(16, bar.getWidth());
        assertEquals(new Dimension(15, 15), bar.getComponent(0).getSize());
        assertEquals(1, bar.getComponent(0).getX());
    }

    /**
     * A header the theme draws is as tall as its text, sorted or not; one drawn by the application's renderer keeps
     * the renderer's height; the cell border keeps its margins either way.
     */
    /** By default a header is as tall as FlatLaf Light's. */
    @Test
    void aHeaderIsAsTallAsFlatLafLightsByDefault() {
        FlatLightLaf.setup();
        int light = new JTable(new javax.swing.table.DefaultTableModel(new Object[]{"Name", "Size"}, 3))
                .getTableHeader().getPreferredSize().height;
        FlatLagoonLaf.setup();
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(new Object[]{"Name", "Size"}, 3));
        assertEquals(light, table.getTableHeader().getPreferredSize().height);
    }

    /** With TableHeader.height 0 (the dense look), a header the theme draws is as tall as its text. */
    @Test
    void aHeaderTheThemeDrawsIsAsTallAsItsText() {
        dense();
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(new Object[]{"Name", "Size"}, 3));
        table.setAutoCreateRowSorter(true);
        int text = table.getFontMetrics(UIManager.getFont("TableHeader.font")).getHeight();
        assertEquals(text, table.getTableHeader().getPreferredSize().height);
        table.getRowSorter().toggleSortOrder(0);
        assertEquals(text, table.getTableHeader().getPreferredSize().height, "sorted");

        JTable own = new JTable(1, 1);
        own.getTableHeader().setDefaultRenderer((t, value, selected, focus, row, column) -> {
            JLabel label = new JLabel(String.valueOf(value));
            label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            label.setPreferredSize(new Dimension(40, 22));
            return label;
        });
        assertEquals(22, own.getTableHeader().getPreferredSize().height, "the application's renderer");
        assertEquals(new Insets(1, 3, 1, 3), UIManager.getBorder("TableHeader.cellBorder").getBorderInsets(new JLabel()));
    }

    @Test
    void htmlLinksInLabelsTakeTheThemesLinkColour() {
        FlatLagoonLaf.setup();
        javax.swing.text.html.StyleSheet css = new javax.swing.text.html.HTMLEditorKit().getStyleSheet();
        assertEquals(new Color(0x0033CC), css.getForeground(css.getRule("a")));
    }

    /** A theme that puts its own base font into UIManager and takes it back when it is switched away from. */
    public static final class FontOwningTheme extends FlatLightLaf {
        static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        @Override
        public void initialize() {
            UIManager.put("defaultFont", FONT);
            super.initialize();
        }

        @Override
        public void uninitialize() {
            UIManager.put("defaultFont", null);
            super.uninitialize();
        }
    }

    @Test
    void switchingFromAThemeThatTakesItsFontBackBasesFontsOnDialog12() throws Exception {
        UIManager.setLookAndFeel(new FontOwningTheme());
        assertEquals(Font.SANS_SERIF, UIManager.getFont("Label.font").getName());
        dense();
        assertEquals("Dialog", UIManager.getFont("Label.font").getName());
    }

    @Test
    void switchingAwayTakesTheBaseFontBackOutOfUIManager() {
        dense();
        FlatLightLaf.setup();
        // Nothing left above the new theme's own defaults: UIManager answers with the theme's font.
        assertTrue(UIManager.get("defaultFont") == UIManager.getLookAndFeelDefaults().get("defaultFont"));
    }

    // ---- defaults: FlatLaf Light's layout and sizes ------------------------------------------------------

    private static final String[] LIGHTS_LAYOUT = {
            "TextField.margin", "FormattedTextField.margin", "PasswordField.margin", "TextArea.margin",
            "ComboBox.padding", "Spinner.padding", "Table.rowHeight", "Table.cellMargins", "List.cellMargins",
            "Tree.rowHeight", "Tree.paintLines", "Tree.showDefaultIcons", "Tree.border", "Tree.rendererMargins",
            "Tree.leftChildIndent", "Tree.rightChildIndent", "Component.arrowType", "TabbedPane.hasFullBorder",
            "OptionPane.border", "OptionPane.buttonAreaBorder", "OptionPane.iconMessageGap",
            "OptionPane.buttonOrientation", "OptionPane.isYesLast", "OptionPane.buttonMinimumWidth",
            "OptionPane.setButtonMargin", "OptionPane.maxCharactersPerLine", "OptionPane.sameSizeButtons",
            "Panel.background", "defaultFont", "Label.font", "Button.margin", "CheckBox.margin",
            "RadioButton.margin", "TabbedPane.tabHeight", "TabbedPane.tabInsets", "TabbedPane.tabAreaInsets",
            "TableHeader.height", "TableHeader.cellMargins", "ProgressBar.horizontalSize", "ProgressBar.verticalSize"};

    /**
     * Paddings, row heights, trees, arrows, tab frame and sizes, button, check box and radio margins, table header,
     * progress bar, message layout, panel colour and font: Light's.
     */
    @Test
    void layoutSizesFontAndPanelColourAreFlatLafLights() {
        FlatLightLaf.setup();
        java.util.Map<String, Object> light = new java.util.LinkedHashMap<>();
        for (String key : LIGHTS_LAYOUT) {
            light.put(key, UIManager.get(key));
        }
        FlatLagoonLaf.setup();
        for (String key : LIGHTS_LAYOUT) {
            Object lagoon = UIManager.get(key);
            if (lagoon instanceof javax.swing.border.EmptyBorder) {
                assertEquals(((Border) light.get(key)).getBorderInsets(new JPanel()),
                        ((Border) lagoon).getBorderInsets(new JPanel()), key);
            } else {
                assertEquals(light.get(key), lagoon, key);
            }
        }
        assertEquals(new Color(0xF2F2F2), UIManager.getColor("Panel.background"));
    }

    /** A dense application keeps the classic values with its own custom defaults. */
    @Test
    void anApplicationsCustomDefaultsKeepTheDenseLook() {
        dense();
        assertEquals(new Color(0xF5F6F7), UIManager.getColor("Panel.background"));
        assertEquals(new Insets(1, 1, 1, 1), UIManager.getInsets("TextField.margin"));
        assertEquals(new Insets(1, 1, 1, 1), UIManager.getInsets("Table.cellMargins"));
        assertEquals(16, UIManager.getInt("Table.rowHeight"));
        assertTrue(UIManager.getBoolean("TabbedPane.hasFullBorder"));
        assertEquals("triangle", UIManager.getString("Component.arrowType"));
        assertEquals(new Insets(3, 14, 3, 14), UIManager.getInsets("Button.margin"));
        assertEquals(new Insets(4, 5, 4, 5), UIManager.getInsets("RadioButton.margin"));
        assertEquals(20, UIManager.getInt("TabbedPane.tabHeight"));
        assertEquals(new Insets(1, 2, 0, 0), UIManager.getInsets("TabbedPane.tabAreaInsets"));
        assertEquals(0, UIManager.getInt("TableHeader.height"));
        assertEquals(new Insets(1, 3, 1, 3), UIManager.getInsets("TableHeader.cellMargins"));
        assertEquals(new java.awt.Dimension(146, 14), UIManager.getDimension("ProgressBar.horizontalSize"));
    }

    /** By default no frames: Light's plain track and bar. */
    @Test
    void aProgressBarIsUnframedByDefault() {
        FlatLagoonLaf.setup();
        javax.swing.JProgressBar progress = new javax.swing.JProgressBar(0, 100);
        progress.setValue(50);
        progress.setSize(100, progress.getPreferredSize().height);
        int[] row = progressRow(progress);
        assertEquals(UIManager.getColor("ProgressBar.foreground").getRGB(), row[0]);
        assertEquals(UIManager.getColor("ProgressBar.background").getRGB(), row[99]);
    }

    /** With the frame keys the track is framed and the bar sits a pixel inside it with its own frame, busy too. */
    @Test
    void aProgressBarsTrackAndBarAreFramedWithTheFrameKeys() {
        dense();
        int frame = UIManager.getColor("ProgressBar.borderColor").getRGB();
        int track = UIManager.getColor("ProgressBar.background").getRGB();
        int barFrame = UIManager.getColor("ProgressBar.barBorderColor").getRGB();
        int bar = UIManager.getColor("ProgressBar.foreground").getRGB();
        javax.swing.JProgressBar progress = new javax.swing.JProgressBar(0, 100);
        progress.setValue(50);
        progress.setSize(100, progress.getPreferredSize().height);
        assertEquals(UIManager.getDimension("ProgressBar.horizontalSize").height + 4, progress.getHeight());
        int[] row = progressRow(progress);
        // frame, gap (track), bar frame, bar ... and at the right: track, frame.
        assertEquals(frame, row[0]);
        assertEquals(track, row[1]);
        assertEquals(barFrame, row[2]);
        assertEquals(bar, row[3]);
        assertEquals(track, row[96]);
        assertEquals(frame, row[99]);
        progress.setIndeterminate(true);
        assertTrue(java.util.Arrays.stream(progressRow(progress)).anyMatch(rgb -> rgb == barFrame), "a framed box while busy");
    }

    private static int[] progressRow(javax.swing.JProgressBar progress) {
        BufferedImage image = new BufferedImage(progress.getWidth(), progress.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        progress.paint(g);
        g.dispose();
        int[] row = new int[image.getWidth()];
        for (int x = 0; x < row.length; x++) {
            row[x] = image.getRGB(x, image.getHeight() / 2);
        }
        return row;
    }

    /** Light's font after a live switch, as an application with a theme menu does it; Dialog 12 with the switch. */
    @Test
    void aLiveSwitchFromLightKeepsThePlatformFontAndTheSwitchGivesDialog12() {
        FlatLightLaf.setup();
        JLabel label = new JLabel("Orders");
        Font light = label.getFont();
        FlatLaf.setup(new FlatLagoonLaf());
        SwingUtilities.updateComponentTreeUI(label);
        assertEquals(light, label.getFont());

        FlatLaf.registerCustomDefaultsSource(DENSE);
        FlatLaf.setup(new FlatLagoonLaf());
        SwingUtilities.updateComponentTreeUI(label);
        assertEquals(new Font(Font.DIALOG, Font.PLAIN, 12), new Font(label.getFont().getName(),
                label.getFont().getStyle(), label.getFont().getSize()));
    }

    /** Without the switch, a label is as wide as its text, and a text-only tree row as Swing's renderer makes it. */
    @Test
    void labelsAndTreeRowsTakeFlatLafsSizeWithoutTheGapSwitch() {
        FlatLagoonLaf.setup();
        JLabel label = new JLabel("Username");
        assertEquals(label.getFontMetrics(label.getFont()).stringWidth("Username"), label.getPreferredSize().width);
        javax.swing.tree.DefaultTreeCellRenderer renderer = new javax.swing.tree.DefaultTreeCellRenderer();
        renderer.getTreeCellRendererComponent(settingsLikeTree(), "Parent", false, false, true, 0, false);
        renderer.setIcon(null);
        Insets in = renderer.getInsets();
        assertEquals(renderer.getFontMetrics(renderer.getFont()).stringWidth("Parent") + 3 + in.left + in.right,
                renderer.getPreferredSize().width);
    }

    /** Disabled fields are as grey as the panel they sit on. */
    @Test
    void aDisabledFieldIsGreyLikeThePanel() {
        FlatLagoonLaf.setup();
        assertEquals(UIManager.getColor("Panel.background"), UIManager.getColor("TextField.disabledBackground"));
        assertEquals(UIManager.getColor("Panel.background"), UIManager.getColor("ComboBox.disabledBackground"));
    }

    /** A focused table, list or tree shows its selection in the accent with white text; without focus, pale. */
    @Test
    void aSelectionIsStrongWithFocusAndPaleWithout() {
        FlatLagoonLaf.setup();
        for (String c : new String[]{"Table", "List", "Tree"}) {
            assertEquals(new Color(0x2A80D9), UIManager.getColor(c + ".selectionBackground"), c);
            assertEquals(Color.WHITE, UIManager.getColor(c + ".selectionForeground"), c);
            assertEquals(new Color(0xB2D8FF), UIManager.getColor(c + ".selectionInactiveBackground"), c);
            assertEquals(Color.BLACK, UIManager.getColor(c + ".selectionInactiveForeground"), c);
        }
        // the focused cell's box stands out on the strong selection
        assertTrue(!UIManager.getColor("Table.cellFocusColor").equals(UIManager.getColor("Table.selectionBackground")));
        assertEquals(new Color(0xB2D8FF), UIManager.getColor("MenuItem.selectionBackground"), "menus keep the pale one");
        assertEquals(Color.BLACK, UIManager.getColor("MenuItem.acceleratorSelectionForeground"));
    }

    /** A focused field, combo box or spinner shows it: its frame turns the focus colour, two pixels thick. */
    @Test
    void aFocusedFieldShowsItsFocusInItsFrame() {
        FlatLagoonLaf.setup();
        int focus = UIManager.getColor("Component.focusColor").getRGB();
        int frame = UIManager.getColor("Component.borderColor").getRGB();
        int[] focused = frameLeftEdge(new JTextField(10) {
            @Override
            public boolean hasFocus() {
                return true;
            }
        });
        assertEquals(UIManager.getColor("Component.ringShadowColor").getRGB(), focused[0], "the ring stays");
        assertEquals(focus, focused[1]);
        assertEquals(focus, focused[2]);
        assertEquals(frame, frameLeftEdge(new JTextField(10))[1]);
        assertEquals(focus, frameLeftEdge(new JComboBox<String>(new String[]{"x"}) {
            @Override
            public boolean hasFocus() {
                return true;
            }
        })[1]);

        dense();
        int[] classic = frameLeftEdge(new JTextField(10) {
            @Override
            public boolean hasFocus() {
                return true;
            }
        });
        assertEquals(frame, classic[1], "the dense look keeps its dark frame");
        assertTrue(classic[2] != frame, "one pixel");
    }

    /** The first pixels of the middle row of a component's border, painted on its own. */
    private static int[] frameLeftEdge(javax.swing.JComponent c) {
        c.setSize(120, 24);
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        c.getBorder().paintBorder(c, g, 0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        return new int[]{image.getRGB(0, 12), image.getRGB(1, 12), image.getRGB(2, 12)};
    }

    /**
     * A button an application colours through FlatLaf styling (a primary action) shows its colour and its own
     * text colour; the theme's raised face is for buttons in the theme's colour.
     */
    @Test
    void aStyledPrimaryButtonShowsItsOwnColour() {
        FlatLagoonLaf.setup();
        Color accent = new Color(0x2675BF);
        JButton primary = new JButton("Checkout");
        primary.putClientProperty("FlatLaf.style", "background: #2675BF; foreground: #ffffff");
        assertEquals(accent.getRGB(), faceAt(primary));

        JButton def = new JButton("Next") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        def.putClientProperty("FlatLaf.style", "default.background: #2675BF; default.foreground: #ffffff");
        assertEquals(accent.getRGB(), faceAt(def), "a styled default button");

        JButton own = new JButton("Own");
        own.setBackground(Color.ORANGE);
        assertEquals(Color.ORANGE.getRGB(), faceAt(own));

        JButton plain = new JButton("Save");
        assertTrue(faceAt(plain) != UIManager.getColor("Button.background").getRGB(), "a plain button keeps the sheen");
    }

    /** The colour a little inside the left edge, half way down. */
    private static int faceAt(JButton b) {
        b.setSize(b.getPreferredSize());
        BufferedImage image = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        b.paint(g);
        g.dispose();
        return image.getRGB(5, b.getHeight() / 2);
    }

    /** Combo boxes and spinners show FlatLaf's chevrons; the switch gives them the small raised buttons. */
    @Test
    void comboBoxesAndSpinnersShowChevronsAndTheSwitchRaisedButtons() {
        FlatLagoonLaf.setup();
        assertTrue(!(arrowOf(new JComboBox<>(new String[]{"x"})) instanceof LagoonArrowButton));
        assertTrue(!(arrowOf(new JSpinner()) instanceof LagoonArrowButton));
        dense();
        assertTrue(arrowOf(new JComboBox<>(new String[]{"x"})) instanceof LagoonArrowButton);
        assertTrue(arrowOf(new JSpinner()) instanceof LagoonArrowButton);
    }

    private static java.awt.Component arrowOf(java.awt.Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof javax.swing.AbstractButton) {
                return child;
            }
        }
        throw new AssertionError("no arrow button in " + c);
    }

    /** A tab's content sits under one line, not in a second box: its own frames are the only ones inside. */
    @Test
    void tabContentIsNotBoxedASecondTime() {
        FlatLagoonLaf.setup();
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab("Items", new JPanel());
        tabs.setSize(300, 200);
        tabs.doLayout();
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        tabs.paint(g);
        g.dispose();
        int line = UIManager.getColor("TabbedPane.contentAreaColor").getRGB();
        assertTrue(image.getRGB(0, 150) != line, "no frame down the left side");
        assertTrue(image.getRGB(150, 199) != line, "no frame along the bottom");
    }

    /** The default button is framed like any other, in one line, and as large as a plain button. */
    @Test
    void aDefaultButtonHasAOnePixelFrameAndAPlainButtonsSize() {
        FlatLagoonLaf.setup();
        JButton def = new JButton("Save") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };
        assertEquals(new JButton("Save").getPreferredSize(), def.getPreferredSize());
        def.setSize(def.getPreferredSize());
        BufferedImage image = new BufferedImage(def.getWidth(), def.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        def.paint(g);
        g.dispose();
        int frame = UIManager.getColor("Button.default.borderColor").getRGB();
        int middle = def.getHeight() / 2;
        assertEquals(frame, image.getRGB(1, middle), "the frame");
        assertTrue(image.getRGB(2, middle) != frame, "one pixel");
    }
}
