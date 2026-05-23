package src.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormResetUtils {

    public static void resetFeesForm(
            JComboBox<?> dropdown,
            ButtonGroup group,
            JTextField transactionField,
            JLabel transactionLabel,
            DefaultTableModel model,
            JLabel total,
            JLabel discount,
            JLabel payable
    ) {

        dropdown.setSelectedIndex(0);

        group.clearSelection();

        transactionField.setText("");

        transactionField.setVisible(false);

        transactionLabel.setVisible(false);

        model.setRowCount(0);

        total.setText("₹ 0.00");

        discount.setText("—");

        payable.setText("₹ 0.00");
    }
}