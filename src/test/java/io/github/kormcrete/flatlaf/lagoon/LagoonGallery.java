package io.github.kormcrete.flatlaf.lagoon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * One of everything, in a window, for looking at the theme. Run it as a program; the argument picks the theme:
 * {@code soft}, {@code dark}, {@code soft-dark}, or none for {@link FlatLagoonLaf}.
 */
public class LagoonGallery {

    public static void main(String[] args) {
        String theme = args.length > 0 ? args[0] : "";
        switch (theme) {
            case "soft": FlatLagoonSoftLaf.setup(); break;
            case "dark": FlatLagoonDarkLaf.setup(); break;
            case "soft-dark": FlatLagoonSoftDarkLaf.setup(); break;
            default: FlatLagoonLaf.setup(); break;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lagoon gallery");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(gallery());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static JComponent gallery() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6); c.anchor = GridBagConstraints.NORTHWEST; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;

        JMenuBar mb = new JMenuBar();
        for (String s : new String[]{"File","Edit","View","Help"}) { JMenu m = new JMenu(s); m.add(new JMenuItem("Item")); mb.add(m); }
        row(root, c, "JMenuBar", mb);

        JToolBar tb = new JToolBar();
        tb.add(new JButton("New")); tb.add(new JButton("Save")); tb.addSeparator(); tb.add(new JToggleButton("Filter"));
        row(root, c, "JToolBar", tb);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton def = new JButton("OK (default)");
        JButton norm = new JButton("Cancel");
        JButton dis = new JButton("Disabled"); dis.setEnabled(false);
        JButton armed = new JButton("Pressed"); armed.getModel().setArmed(true); armed.getModel().setPressed(true);
        JButton roll = new JButton("Rollover"); roll.getModel().setRollover(true);
        JToggleButton tog = new JToggleButton("Toggle on"); tog.setSelected(true);
        btns.add(def); btns.add(norm); btns.add(dis); btns.add(armed); btns.add(roll); btns.add(tog);
        row(root, c, "JButton", btns);

        JPanel chk = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JCheckBox c1 = new JCheckBox("Checked", true); JCheckBox c2 = new JCheckBox("Unchecked"); JCheckBox c3 = new JCheckBox("Disabled", true); c3.setEnabled(false);
        JRadioButton r1 = new JRadioButton("Radio on", true); JRadioButton r2 = new JRadioButton("Radio off");
        chk.add(c1); chk.add(c2); chk.add(c3); chk.add(r1); chk.add(r2);
        row(root, c, "Check/Radio", chk);

        JPanel txt = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JTextField t1 = new JTextField("Some text", 18);
        JTextField t2 = new JTextField("focused", 10);
        JTextField t3 = new JTextField("read only", 10); t3.setEditable(false);
        JTextField t4 = new JTextField("disabled", 10); t4.setEnabled(false);
        JPasswordField pw = new JPasswordField("secret", 8);
        txt.add(t1); txt.add(t2); txt.add(t3); txt.add(t4); txt.add(pw);
        row(root, c, "JTextField", txt);

        JPanel cmb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JComboBox<String> cb1 = new JComboBox<>(new String[]{"Red","Green","Blue"});
        JComboBox<String> cb2 = new JComboBox<>(new String[]{"editable"}); cb2.setEditable(true); cb2.setPrototypeDisplayValue("editable  ");
        JComboBox<String> cb3 = new JComboBox<>(new String[]{"disabled"}); cb3.setEnabled(false);
        JSpinner sp = new JSpinner(new SpinnerNumberModel(42, 0, 100, 1));
        cmb.add(cb1); cmb.add(cb2); cmb.add(cb3); cmb.add(sp);
        row(root, c, "JComboBox/JSpinner", cmb);

        JTabbedPane tabs = new JTabbedPane();
        for (String s : new String[]{"General","Details","History","Files"}) { JPanel p = new JPanel(); p.setPreferredSize(new Dimension(500, 60)); p.add(new JLabel(s + " content")); tabs.addTab(s, p); }
        tabs.setSelectedIndex(1);
        row(root, c, "JTabbedPane", tabs);
        JTabbedPane tabsRight = new JTabbedPane(JTabbedPane.RIGHT);
        for (String s : new String[]{"First","Second tab"}) { JPanel p = new JPanel(); p.setPreferredSize(new Dimension(300, 70)); tabsRight.addTab(s, p); }
        row(root, c, "JTabbedPane RIGHT", tabsRight);

        DefaultTableModel tm = new DefaultTableModel(new Object[][]{{"A-001","Red","2026-01-12","open"},{"A-002","Green","2026-01-13","done"},{"A-003","Blue","2026-01-14","open"},{"A-004","Red","2026-01-15","done"}}, new Object[]{"Id","Colour","Date","State"});
        JTable table = new JTable(tm); table.setRowSelectionInterval(1,1);
        JScrollPane tsp = new JScrollPane(table); tsp.setPreferredSize(new Dimension(520, 110));
        row(root, c, "JTable", tsp);

        JPanel lt = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JList<String> list = new JList<>(new String[]{"Item 1","Item 2","Item 3","Item 4"}); list.setSelectedIndex(1);
        JScrollPane lsp = new JScrollPane(list); lsp.setPreferredSize(new Dimension(160, 90));
        JTree tree = new JTree(); tree.setSelectionRow(1); tree.expandRow(1);
        JScrollPane trsp = new JScrollPane(tree); trsp.setPreferredSize(new Dimension(200, 90));
        JTextArea ta = new JTextArea("Notes\nLine 2\nLine 3\nLine 4\nLine 5\nLine 6"); JScrollPane tasp = new JScrollPane(ta); tasp.setPreferredSize(new Dimension(160, 90)); tasp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); tasp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        lt.add(lsp); lt.add(trsp); lt.add(tasp);
        row(root, c, "List/Tree/ScrollBar", lt);

        JPanel misc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JProgressBar pb = new JProgressBar(0,100); pb.setValue(60); pb.setStringPainted(true); pb.setPreferredSize(new Dimension(160, 18));
        JSlider sl = new JSlider(0,100,40); sl.setPreferredSize(new Dimension(160, 30));
        JPanel titled = new JPanel(); titled.setBorder(BorderFactory.createTitledBorder("TitledBorder")); titled.add(new JLabel("Label")); titled.add(new JLabel("Disabled")); ((JLabel)titled.getComponent(1)).setEnabled(false);
        misc.add(pb); misc.add(sl); misc.add(titled);
        row(root, c, "Progress/Slider/Titled", misc);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JLabel(" left "), new JLabel(" right ")); split.setPreferredSize(new Dimension(300, 40)); split.setDividerLocation(150);
        JInternalFrame ifr = new JInternalFrame("JInternalFrame", true, true, true, true); ifr.setSize(260, 80); ifr.setVisible(true);
        JPanel ifp = new JPanel(null); ifp.setPreferredSize(new Dimension(260, 80)); ifp.add(ifr);
        JPanel sp2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); sp2.add(split); sp2.add(ifp);
        row(root, c, "Split/InternalFrame", sp2);

        JPopupMenu pop = new JPopupMenu(); pop.add(new JMenuItem("Open")); pop.add(new JCheckBoxMenuItem("Active", true)); pop.addSeparator(); pop.add(new JMenuItem("Delete"));
        JMenu sub = new JMenu("Submenu"); sub.add(new JMenuItem("x")); pop.add(sub);
        JMenuItem sel = new JMenuItem("Selected"); sel.setArmed(true); pop.add(sel);
        JToolTip tip = new JToolTip(); tip.setTipText("Tooltip text");
        JPanel popp = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); popp.add(pop); popp.add(tip);
        row(root, c, "PopupMenu/ToolTip", popp);

        return root;
    }

    static void row(JPanel root, GridBagConstraints c, String label, JComponent comp) {
        c.gridx = 0; c.weightx = 0; JLabel l = new JLabel(label); l.setFont(l.getFont().deriveFont(Font.BOLD));
        // slack on the right: text painted at a higher scale runs a little wider than its 1x layout
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6)); root.add(l, c);
        c.gridx = 1; c.weightx = 1; root.add(comp, c);
        c.gridy++;
    }
}
