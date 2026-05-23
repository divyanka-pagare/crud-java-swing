package src.components;

import javax.swing.*;
import java.awt.*;

public class ModernScrollPane extends JScrollPane {

    public ModernScrollPane(JTable table) {

        super(table);

        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 225, 230)
                        ),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                )
        );

        getViewport().setBackground(Color.WHITE);

        setBackground(Color.WHITE);

        setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        // Smooth modern scrollbar UI
        getVerticalScrollBar().setUnitIncrement(16);
    }
}