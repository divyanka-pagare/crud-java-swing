package src.db;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class DBConnection {

    private static Connection con = null;

    public static Connection getConnection() {

        if (con != null) return con;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentdb",
                "root",
                "Ritesh@07"
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "DB Connection Failed:\n" + e.getMessage());
            e.printStackTrace();
        }

        return con;
    }
}