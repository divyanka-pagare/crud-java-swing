package src.forms.master;

import src.db.DBConnection;
import src.models.Teacher;
import src.repositories.TeacherRepository;
import src.utils.TableUtils;
import src.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class TeacherForm extends JFrame {

    JTextField  txtName, txtExperience, txtSpecialization;
    JComboBox<String> timeBox;
    JButton     addBtn, updateBtn, deleteBtn, clearBtn;
    JTable      table;
    DefaultTableModel model;

    int selectedRow = -1;

    Connection         con;
    TeacherRepository  repo;

    public TeacherForm() {

        setTitle("Teacher Master");
        setSize(950, 620);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con  = DBConnection.getConnection();
        repo = new TeacherRepository(con);

        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 247, 250));

        JLabel title = UIUtils.bold("Teacher Master", 26);
        title.setBounds(30, 12, 400, 40);
        main.add(title);

        // ── Form ──
        addLabel(main, "Full Name*:",        30, 68);
        addLabel(main, "Experience (yrs)*:", 30, 118);
        addLabel(main, "Specialization*:",   30, 168);
        addLabel(main, "Available Time*:",   30, 218);

        txtName           = field(220, 68);
        txtExperience     = field(220, 118);
        txtSpecialization = field(220, 168);

        String[] times = {
            "Morning (8am-12pm)",
            "Afternoon (12pm-4pm)",
            "Evening (4pm-8pm)",
            "Full Day"
        };
        timeBox = new JComboBox<>(times);
        timeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeBox.setBounds(220, 218, 260, 32);

        main.add(txtName);
        main.add(txtExperience);
        main.add(txtSpecialization);
        main.add(timeBox);

        // ── Buttons ──
        addBtn    = UIUtils.colorButton("ADD",    new Color(0,120,215),   30, 270, 110, 38);
        updateBtn = UIUtils.colorButton("UPDATE", new Color(40,167,69),  155, 270, 110, 38);
        deleteBtn = UIUtils.colorButton("DELETE", new Color(220,53,69),  280, 270, 110, 38);
        clearBtn  = UIUtils.colorButton("CLEAR",  new Color(108,117,125),405, 270, 110, 38);

        main.add(addBtn);
        main.add(updateBtn);
        main.add(deleteBtn);
        main.add(clearBtn);

        // ── Table ──
        String[] cols = {"ID", "Name", "Experience", "Specialization", "Available Time"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = TableUtils.createStyledTable(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(30, 328, 950, 250);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        main.add(scroll);

        add(main);

        loadTable();

        addBtn   .addActionListener(e -> addTeacher());
        updateBtn.addActionListener(e -> updateTeacher());
        deleteBtn.addActionListener(e -> deleteTeacher());
        clearBtn .addActionListener(e -> clearForm());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedRow = table.getSelectedRow();
                txtName          .setText(model.getValueAt(selectedRow, 1).toString());
                txtExperience    .setText(model.getValueAt(selectedRow, 2).toString());
                txtSpecialization.setText(model.getValueAt(selectedRow, 3).toString());
                timeBox.setSelectedItem(model.getValueAt(selectedRow, 4).toString());
            }
        });

        setVisible(true);
    }

    private void addTeacher() {
        String name   = txtName.getText().trim();
        String expStr = txtExperience.getText().trim();
        String spec   = txtSpecialization.getText().trim();
        String time   = timeBox.getSelectedItem().toString();

        if (name.isEmpty() || expStr.isEmpty() || spec.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required"); return; }

        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this, "Name should contain only alphabets"); return; }

        int exp;
        try { exp = Integer.parseInt(expStr); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Experience must be a number"); return; }

        try {
            repo.insert(new Teacher(0, name, exp, spec, time));
            JOptionPane.showMessageDialog(this, "Teacher added successfully");
            loadTable(); clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateTeacher() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first"); return; }

        int    id     = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        String name   = txtName.getText().trim();
        String expStr = txtExperience.getText().trim();
        String spec   = txtSpecialization.getText().trim();
        String time   = timeBox.getSelectedItem().toString();

        if (name.isEmpty() || expStr.isEmpty() || spec.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required"); return; }

        int exp;
        try { exp = Integer.parseInt(expStr); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Experience must be a number"); return; }

        try {
            repo.update(new Teacher(id, name, exp, spec, time));
            JOptionPane.showMessageDialog(this, "Teacher updated successfully");
            loadTable(); clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteTeacher() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this teacher?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        try {
            repo.delete(id);
            JOptionPane.showMessageDialog(this, "Teacher deleted");
            loadTable(); clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadTable() {
        model.setRowCount(0);
        try {
            List<Teacher> list = repo.getAll();
            for (Teacher t : list) {
                model.addRow(new Object[]{
                    t.getId(),
                    t.getName(),
                    t.getExperience() + " yrs",
                    t.getSpecialization(),
                    t.getAvailableTime()
                });
            }
            TableUtils.resizeColumnWidth(table);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void clearForm() {
        txtName.setText("");
        txtExperience.setText("");
        txtSpecialization.setText("");
        timeBox.setSelectedIndex(0);
        selectedRow = -1;
        table.clearSelection();
    }

    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = UIUtils.plain(text, 13);
        l.setBounds(x, y, 180, 28);
        p.add(l);
    }

    private JTextField field(int x, int y) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBounds(x, y, 260, 32);
        return f;
    }
}