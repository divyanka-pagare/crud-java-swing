package src.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class UIUtils {

    // ===== COLORS =====
    public static final Color BG_PAGE       = new Color(245, 247, 250);
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BG_HEADER     = new Color(0, 102, 204);
    public static final Color CLR_BLUE      = new Color(0, 120, 215);
    public static final Color CLR_GREEN     = new Color(40, 167, 69);
    public static final Color CLR_RED       = new Color(220, 53, 69);
    public static final Color CLR_GRAY      = new Color(108, 117, 125);
    public static final Color CLR_DARK      = new Color(52, 58, 64);
    public static final Color CLR_PURPLE    = new Color(153, 0, 153);
    public static final Color BORDER_COLOR  = new Color(210, 215, 220);
    public static final Font  FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font  FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_BTN      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font  FONT_TABLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 13);

    // ===== LABELS =====
    public static JLabel bold(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size));
        return l;
    }

    public static JLabel plain(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, size));
        return l;
    }

    public static JLabel info(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(80, 80, 90));
        return l;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        return l;
    }

    // ===== BUTTONS =====
    public static JButton colorButton(String text, Color bg,
                                       int x, int y, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBounds(x, y, w, h);
        b.setBorderPainted(false);
        return b;
    }

    // ===== TEXT FIELDS =====
    public static JTextField textField(int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setBounds(x, y, w, h);
        return f;
    }

    public static JPasswordField passwordField(int x, int y, int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setBounds(x, y, w, h);
        return f;
    }

    public static JTextArea textArea(int x, int y, int w, int h) {
        JTextArea a = new JTextArea();
        a.setFont(FONT_INPUT);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBounds(x, y, w, h);
        return a;
    }

    // ===== COMBO BOX =====
    public static JComboBox<String> comboBox(String[] items,
                                              int x, int y, int w, int h) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT_INPUT);
        c.setBounds(x, y, w, h);
        return c;
    }

    // ===== CARDS =====
    public static JPanel card(String title, int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        p.setBounds(x, y, w, h);
        return p;
    }

    // ===== MAIN PANEL =====
    public static JPanel mainPanel() {
        JPanel p = new JPanel(null);
        p.setBackground(BG_PAGE);
        return p;
    }

    // ===== PLACEHOLDER =====
    public static void addPlaceholder(JTextComponent field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    // ===== OPEN FULLSCREEN =====
    public static void openFullScreen(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    // ===== TABLE STYLING =====
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(36);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // KEY CHANGE: use ALL_COLUMNS so table fills container width
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(BG_HEADER);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    // ===== RESIZE COLUMNS TO CONTENT =====
    public static void resizeColumns(JTable table) {

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel cm = table.getColumnModel();

        int totalWidth = 0;

        int[] widths = new int[table.getColumnCount()];

        for (int col = 0; col < table.getColumnCount(); col++) {

            int width = 60;

            // header
            TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
            Component hc = hr.getTableCellRendererComponent(
                table, cm.getColumn(col).getHeaderValue(),
                false, false, 0, col);
            width = Math.max(width, hc.getPreferredSize().width + 24);

            // rows
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer r = table.getCellRenderer(row, col);
                Component c = table.prepareRenderer(r, row, col);
                width = Math.max(width, c.getPreferredSize().width + 24);
            }
            
            widths[col] = width;
            totalWidth += width;

        }

        // get available table width
        int available = table.getParent() != null
            ? table.getParent().getWidth()
            : table.getWidth();

        if (available <= 0) available = totalWidth;

        // if content fits in available width — stretch to fill
        // if content is wider than available — use content width with scroll
        if (totalWidth <= available) {
            // distribute extra space proportionally
            int extra = available - totalWidth;
            for (int col = 0; col < table.getColumnCount(); col++) {
                int bonus = (int)((double) widths[col] / totalWidth * extra);
                cm.getColumn(col).setPreferredWidth(widths[col] + bonus);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            // content too wide — keep content widths, allow scroll
            for (int col = 0; col < table.getColumnCount(); col++) {
                cm.getColumn(col).setPreferredWidth(widths[col]);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }
    

    // ===== ZEBRA TABLE =====
    public static JTable zebraTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer r,
                                              int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0
                        ? Color.WHITE
                        : new Color(245, 247, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        };
        styleTable(table);
        return table;
    }

    // ===== SCROLL PANE =====
    public static JScrollPane scrollPane(JTable table,
                                          int x, int y, int w, int h) {
        JScrollPane sp = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBounds(x, y, w, h);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }
}