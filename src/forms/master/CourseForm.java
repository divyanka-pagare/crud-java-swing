package src.forms.master;

import src.db.DBConnection;
// import src.components.ModernButton;
import src.models.Course;

import src.repositories.CourseRepository;

import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class CourseForm extends JFrame {

    JTextField txtName, txtFees, txtDuration;
    JButton    addBtn, updateBtn, deleteBtn, clearBtn;
    JTable     table;
    DefaultTableModel model;

    int selectedRow = -1;

    Connection        con;
    CourseRepository  repo;
    
    public CourseForm() {

        setTitle("Course Master");
        setSize(1200, 720);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    
        con = DBConnection.getConnection();
        repo = new CourseRepository(con);
    
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(245, 247, 250));
    
        // ================= LEFT PANEL =================
    
        JPanel leftPanel = new JPanel(null);
        leftPanel.setPreferredSize(new Dimension(420, 600));
        leftPanel.setBackground(new Color(245, 247, 250));
    
        JLabel title = UIUtils.bold("Course Master", 28);
        title.setBounds(20, 10, 300, 40);
        leftPanel.add(title);
    
        addLabel(leftPanel, "Course Name*:", 20, 80);
        addLabel(leftPanel, "Duration*:", 20, 145);
        addLabel(leftPanel, "Fees (₹)*:", 20, 210);
    
        txtName = field(20, 110);
        txtDuration = field(20, 175);
        txtFees = field(20, 240);
    
        leftPanel.add(txtName);
        leftPanel.add(txtDuration);
        leftPanel.add(txtFees);
    
        addBtn = UIUtils.colorButton(
                "ADD",
                new Color(0, 120, 215),
                20, 320, 150, 42);
    
        updateBtn = UIUtils.colorButton(
                "UPDATE",
                new Color(40, 167, 69),
                190, 320, 150, 42);
    
        deleteBtn = UIUtils.colorButton(
                "DELETE",
                new Color(220, 53, 69),
                20, 385, 150, 42);
    
        clearBtn = UIUtils.colorButton(
                "CLEAR",
                new Color(108, 117, 125),
                190, 385, 150, 42);
    
        leftPanel.add(addBtn);
        leftPanel.add(updateBtn);
        leftPanel.add(deleteBtn);
        leftPanel.add(clearBtn);
    
        // ================= RIGHT PANEL =================
    
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
    
        String[] cols = {"ID", "Course Name", "Duration", "Fees (₹)"};
    
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    
        table = TableUtils.createStyledTable(model);
    
        JScrollPane scroll = UIUtils.scrollPane(table, 20, 20, 760, 560);
    
        rightPanel.add(scroll, BorderLayout.CENTER);
    
        // ================= MAIN =================
    
        main.add(leftPanel, BorderLayout.WEST);
        main.add(rightPanel, BorderLayout.CENTER);
    
        add(main);
    
        loadTable();

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    
        // ================= LISTENERS =================
    
        addBtn.addActionListener(e -> addCourse());
        updateBtn.addActionListener(e -> updateCourse());
        deleteBtn.addActionListener(e -> deleteCourse());
        clearBtn.addActionListener(e -> clearForm());
    
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
    
                selectedRow = table.getSelectedRow();
    
                txtName.setText(model.getValueAt(selectedRow, 1).toString());
    
                txtDuration.setText(
                        model.getValueAt(selectedRow, 2).toString());
    
                txtFees.setText(
                        model.getValueAt(selectedRow, 3)
                                .toString()
                                .replace("₹", "")
                                .replace(",", "")
                                .trim());
            }
        });
    
        setVisible(true);
    } 
    
    private void addCourse() {
        String name     = txtName.getText().trim();
        String duration = txtDuration.getText().trim();
        String feesStr  = txtFees.getText().trim();

        if (name.isEmpty() || duration.isEmpty() || feesStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required"); return; }

        double fees;
        try { fees = Double.parseDouble(feesStr); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fees must be a number"); return; }

        try {
            if (repo.existsByName(name, -1)) {
                JOptionPane.showMessageDialog(this, "Course already exists"); return; }
            repo.insert(new Course(0, name, fees, duration));
            JOptionPane.showMessageDialog(this, "Course added successfully");
            loadTable(); clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateCourse() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first"); return; }

        int    id       = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        String name     = txtName.getText().trim();
        String duration = txtDuration.getText().trim();
        String feesStr  = txtFees.getText().trim();

        if (name.isEmpty() || duration.isEmpty() || feesStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required"); return; }

        double fees;
        try { fees = Double.parseDouble(feesStr); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fees must be a number"); return; }

        try {
            if (repo.existsByName(name, id)) {
                JOptionPane.showMessageDialog(this, "Course name already taken"); return; }
            repo.update(new Course(id, name, fees, duration));
            JOptionPane.showMessageDialog(this, "Course updated successfully");
            loadTable(); clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteCourse() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this course?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        try {
            repo.delete(id);
            JOptionPane.showMessageDialog(this, "Course deleted");
            loadTable(); clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete — course may be linked to enrollments");
        }
    }

    private void loadTable() {
        model.setRowCount(0);
        try {
            List<Course> list = repo.getAll();
            for (Course c : list) {
                model.addRow(new Object[]{
                    c.getId(),
                    c.getCourseName(),
                    c.getDuration(),
                    String.format("₹ %,.2f", c.getFees())
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void clearForm() {
        txtName.setText("");
        txtDuration.setText("");
        txtFees.setText("");
        selectedRow = -1;
        table.clearSelection();
    }

    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = UIUtils.plain(text, 13);
        l.setBounds(x, y, 140, 28);
        p.add(l);
    }

    private JTextField field(int x, int y) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBounds(x, y, 260, 32);
        return f;
    }
}