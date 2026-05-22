package src.utils;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class TableUtils {

    public static void styleTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        table.setRowHeight(32);

        table.setGridColor(new Color(230, 230, 230));

        table.setSelectionBackground(new Color(184, 207, 229));

        table.setShowVerticalLines(false);

        table.setIntercellSpacing(new Dimension(0, 0));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        header.setBackground(new Color(0, 102, 204));

        header.setForeground(Color.WHITE);

        ((DefaultTableCellRenderer)
                header.getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center);
        }
    }

    // ===== ZEBRA STRIPES =====
    public static JTable createStyledTable(DefaultTableModel model) {

        JTable table = new JTable(model) {

            @Override
            public Component prepareRenderer(
                    TableCellRenderer renderer,
                    int row,
                    int column) {

                Component c = super.prepareRenderer(
                        renderer,
                        row,
                        column);

                if (!isRowSelected(row)) {

                    c.setBackground(
                            row % 2 == 0
                                    ? Color.WHITE
                                    : new Color(245, 247, 250)
                    );

                } else {

                    c.setBackground(
                            new Color(184, 207, 229)
                    );
                }

                return c;
            }
        };

        styleTable(table);

        return table;
    }

    public static void resizeColumnWidth(JTable table) {

        for (int column = 0;
             column < table.getColumnCount();
             column++) {

            int width = 80;

            for (int row = 0;
                 row < table.getRowCount();
                 row++) {

                TableCellRenderer renderer =
                        table.getCellRenderer(row, column);

                Component comp =
                        table.prepareRenderer(
                                renderer,
                                row,
                                column
                        );

                width = Math.max(
                        comp.getPreferredSize().width + 20,
                        width
                );
            }

            table.getColumnModel()
                    .getColumn(column)
                    .setPreferredWidth(width);
        }
    }
}