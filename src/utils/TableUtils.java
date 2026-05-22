package src.utils;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class TableUtils {

    public static void styleTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        table.setRowHeight(30);

        table.setGridColor(new Color(230, 230, 230));

        table.setSelectionBackground(new Color(184, 207, 229));

        table.setShowVerticalLines(false);

        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        header.setBackground(new Color(0, 102, 204));

        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center);
        }
    }

    public static void resizeColumnWidth(JTable table) {

        for (int column = 0; column < table.getColumnCount(); column++) {

            int width = 80;

            for (int row = 0; row < table.getRowCount(); row++) {

                TableCellRenderer renderer =
                        table.getCellRenderer(row, column);

                Component comp = table.prepareRenderer(
                        renderer, row, column);

                width = Math.max(
                        comp.getPreferredSize().width + 20,
                        width);
            }

            TableColumnModel columnModel =
                    table.getColumnModel();

            columnModel.getColumn(column)
                    .setPreferredWidth(width);
        }
    }
}