package src.utils;

import javax.swing.*;
import javax.swing.table.*;


public class TableUtils {

        
    public static JTable createStyledTable(DefaultTableModel model) {
        return UIUtils.zebraTable(model);
    }

    public static void styleTable(JTable table) {
        UIUtils.styleTable(table);
    }

    public static void resizeColumnWidth(JTable table) {
        UIUtils.resizeColumns(table);
    }

}