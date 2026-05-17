package src.forms;

import src.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationForm extends JFrame {

    JTextField     txtName, txtEmail, txtPhone, txtSearch;
    JTextArea      txtAddress;
    JPasswordField txtPassword;
    JRadioButton   male, female, other;
    ButtonGroup    genderGroup;
    JCheckBox      java, python, webDev, ai;
    JComboBox<String> countryBox;
    JSpinner       ageSpinner;
    JTextArea      bioArea;
    JTable         table;
    DefaultTableModel model;
    JButton        addBtn, updateBtn, deleteBtn, clearBtn;

    int selectedRow = -1;

    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    public RegistrationForm() {

        setTitle("Registration Form - CRUD Operations");
        setSize(1200, 750);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // changed from EXIT so Main stays alive
        setLocationRelativeTo(null);

        connectDB();

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 247, 250));
        panel.setLayout(null);

        // ===== Title =====
        JLabel title = new JLabel("Student Registration Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(330, 10, 600, 50);
        panel.add(title);

        // ===== Labels =====
        addLabel(panel, "Full Name*:",  30,  70);
        addLabel(panel, "Email*:",      30, 120);
        addLabel(panel, "Password*:",   30, 170);
        addLabel(panel, "Phone*:",      30, 220);
        addLabel(panel, "Gender*:",     30, 270);
        addLabel(panel, "Skills*:",     30, 320);
        addLabel(panel, "Country:",     30, 370);
        addLabel(panel, "Age*:",        30, 420);
        addLabel(panel, "Address*:",    30, 470);
        addLabel(panel, "Bio:",         30, 550);

        // ===== Inputs =====
        txtName = new JTextField();
        txtName.setFont(new Font("Arial", Font.PLAIN, 14));
        txtName.setBounds(150, 70, 300, 35);
        panel.add(txtName);
        addPlaceholder(txtName, "Enter full name");

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEmail.setBounds(150, 120, 300, 35);
        panel.add(txtEmail);
        addPlaceholder(txtEmail, "Enter email address");

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 170, 300, 35);
        panel.add(txtPassword);
        addPlaceholder(txtPassword, "Enter password");

        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPhone.setBounds(150, 220, 300, 35);
        panel.add(txtPhone);
        addPlaceholder(txtPhone, "Enter mobile number");

        // ===== Gender =====
        male   = new JRadioButton("Male");
        female = new JRadioButton("Female");
        other  = new JRadioButton("Other");
        male.setBounds(150, 270, 80, 25);
        female.setBounds(250, 270, 90, 25);
        other.setBounds(360, 270, 90, 25);
        genderGroup = new ButtonGroup();
        genderGroup.add(male);
        genderGroup.add(female);
        genderGroup.add(other);
        panel.add(male);
        panel.add(female);
        panel.add(other);

        // ===== Skills =====
        java   = new JCheckBox("Java");
        python = new JCheckBox("Python");
        webDev = new JCheckBox("Web Development");
        ai     = new JCheckBox("AI / ML");
        java.setLocation(150, 320);   java.setSize(java.getPreferredSize());
        python.setLocation(220, 320); python.setSize(python.getPreferredSize());
        webDev.setLocation(300, 320); webDev.setSize(webDev.getPreferredSize());
        ai.setLocation(450, 320);     ai.setSize(ai.getPreferredSize());
        panel.add(java); panel.add(python); panel.add(webDev); panel.add(ai);

        // ===== Country =====
        String[] countries = {"India","USA","Canada","Germany","Australia"};
        countryBox = new JComboBox<>(countries);
        countryBox.setBounds(150, 370, 300, 35);
        panel.add(countryBox);

        // ===== Age =====
        ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 100, 1));
        ageSpinner.setBounds(150, 420, 80, 30);
        panel.add(ageSpinner);

        // ===== Address =====
        txtAddress = new JTextArea();
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(txtAddress,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addressScroll.setBounds(150, 470, 250, 60);
        panel.add(addressScroll);
        addPlaceholder(txtAddress, "Enter full address");

        // ===== Bio =====
        bioArea = new JTextArea();
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setBounds(150, 550, 250, 60);
        panel.add(bioScroll);
        addPlaceholder(bioArea, "Write something about yourself");

        enableDynamicTooltip(txtName);
        enableDynamicTooltip(txtEmail);
        enableDynamicTooltip(txtPhone);
        enableDynamicTooltip(txtPassword);
        enableDynamicTooltip(txtAddress);
        enableDynamicTooltip(bioArea);

        // ===== Buttons =====
        addBtn    = createButton("ADD",    new Color(0, 120, 215),   30, 640);
        updateBtn = createButton("UPDATE", new Color(40, 167, 69),  170, 640);
        deleteBtn = createButton("DELETE", new Color(220, 53, 69),  310, 640);
        clearBtn  = createButton("CLEAR",  new Color(108, 117, 125),450, 640);
        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(clearBtn);

        // ===== Table =====
        String[] columns = {
            "ID","Name","Email","Phone","Gender",
            "Skills","Country","Age","Address","Bio"
        };
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object v = getValueAt(row, col);
                    return v != null ? v.toString() : null;
                }
                return null;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setGridColor(Color.LIGHT_GRAY);
        table.setRowHeight(35);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerR);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(7).setCellRenderer(rightR);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBounds(550, 70, 600, 500);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(tableScroll);

        // ===== Button Actions =====
        addBtn.addActionListener(e -> addRecord());
        updateBtn.addActionListener(e -> updateRecord());
        deleteBtn.addActionListener(e -> deleteRecord());
        clearBtn.addActionListener(e -> clearForm());

        // ===== Button Actions =====
        addBtn.addActionListener(e -> addRecord());
        updateBtn.addActionListener(e -> updateRecord());
        deleteBtn.addActionListener(e -> deleteRecord());
        clearBtn.addActionListener(e -> clearForm());

        // ===== COURSE SELECTION BUTTON =====
        JButton courseBtn = new JButton("Course Selection →");
        courseBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        courseBtn.setBackground(new Color(40, 167, 69));
        courseBtn.setForeground(Color.WHITE);
        courseBtn.setBounds(600, 640, 220, 40);
        panel.add(courseBtn);

        courseBtn.addActionListener(e -> {
            new src.forms.CourseSelectionForm();
        });

        
        // ===== Table Row Click =====
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedRow = table.getSelectedRow();
                txtName.setText(model.getValueAt(selectedRow, 1).toString());
                txtName.setForeground(Color.BLACK);
                txtEmail.setText(model.getValueAt(selectedRow, 2).toString());
                txtEmail.setForeground(Color.BLACK);
                txtPhone.setText(model.getValueAt(selectedRow, 3).toString());
                txtPhone.setForeground(Color.BLACK);

                String gender = model.getValueAt(selectedRow, 4).toString();
                if (gender.equals("Male"))        male.setSelected(true);
                else if (gender.equals("Female")) female.setSelected(true);
                else                              other.setSelected(true);

                String skills = model.getValueAt(selectedRow, 5).toString();
                java.setSelected(skills.contains("Java"));
                python.setSelected(skills.contains("Python"));
                webDev.setSelected(skills.contains("Web Dev"));
                ai.setSelected(skills.contains("AI/ML"));

                countryBox.setSelectedItem(model.getValueAt(selectedRow, 6).toString());
                ageSpinner.setValue(
                    Integer.parseInt(model.getValueAt(selectedRow, 7).toString()));

                txtAddress.setText(model.getValueAt(selectedRow, 8).toString());
                txtAddress.setForeground(Color.BLACK);
                bioArea.setText(model.getValueAt(selectedRow, 9).toString());
                bioArea.setForeground(Color.BLACK);
            }
        });

        add(panel);
        loadTable();
        setVisible(true);
    }

    // ===== HELPER: label =====
    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 120, 25);
        p.add(lbl);
    }

    // ===== HELPER: button =====
    private JButton createButton(String text, Color bg, int x, int y) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBounds(x, y, 120, 40);
        return btn;
    }

    // ===== ADD =====
    public void addRecord() {

        String name  = capitalizeWords(txtName.getText().trim());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || name.equals("Enter full name")) {
            JOptionPane.showMessageDialog(this, "Name is required"); return; }
        if (email.isEmpty() || email.equals("Enter email address")) {
            JOptionPane.showMessageDialog(this, "Email is required"); return; }
        if (String.valueOf(txtPassword.getPassword()).isEmpty() ||
            String.valueOf(txtPassword.getPassword()).equals("Enter password")) {
            JOptionPane.showMessageDialog(this, "Password is required"); return; }
        if (phone.isEmpty() || phone.equals("Enter mobile number")) {
            JOptionPane.showMessageDialog(this, "Phone number is required"); return; }
        if (!male.isSelected() && !female.isSelected() && !other.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select gender"); return; }
        if (!java.isSelected() && !python.isSelected()
                && !webDev.isSelected() && !ai.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select at least one skill"); return; }
        if (txtAddress.getText().trim().isEmpty() ||
            txtAddress.getText().equals("Enter full address")) {
            JOptionPane.showMessageDialog(this, "Address is required"); return; }
        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this, "Name should contain only alphabets"); return; }

        String emailRegex =
            "^[A-Za-z][A-Za-z0-9+_.-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(this, "Invalid Email Address"); return; }
        if (email.length() > 50) {
            JOptionPane.showMessageDialog(this, "Email is too long"); return; }
        if (!phone.matches("[0-9]{10}")) {
            JOptionPane.showMessageDialog(this,
                "Mobile Number must contain exactly 10 digits"); return; }

        String gender = male.isSelected() ? "Male" :
                        female.isSelected() ? "Female" : "Other";

        String skills = "";
        if (java.isSelected())   skills += "Java ";
        if (python.isSelected()) skills += "Python ";
        if (webDev.isSelected()) skills += "Web Dev ";
        if (ai.isSelected())     skills += "AI/ML ";

        String country = countryBox.getSelectedItem().toString();
        int    age     = (Integer) ageSpinner.getValue();
        String address = txtAddress.getText();
        String bio     = bioArea.getText();

        try {
            pst = con.prepareStatement(
                "SELECT * FROM students WHERE email=?");
            pst.setString(1, email);
            rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already exists"); return; }

            pst = con.prepareStatement(
                "INSERT INTO students " +
                "(name,email,`password`,phone,gender,skills,country,age,address,bio) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)");
            pst.setString(1, name);   pst.setString(2, email);
            pst.setString(3, String.valueOf(txtPassword.getPassword()));
            pst.setString(4, phone);  pst.setString(5, gender);
            pst.setString(6, skills); pst.setString(7, country);
            pst.setInt(8, age);       pst.setString(9, address);
            pst.setString(10, bio);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Record Added Successfully");
            loadTable();

        } catch (Exception ex) { ex.printStackTrace(); }

        clearForm();
    }

    // ===== UPDATE =====
    public void updateRecord() {

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select Row First"); return; }

        int    id    = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        String name  = capitalizeWords(txtName.getText().trim());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || name.equals("Enter full name")) {
            JOptionPane.showMessageDialog(this, "Name is required"); return; }
        if (email.isEmpty() || email.equals("Enter email address")) {
            JOptionPane.showMessageDialog(this, "Email is required"); return; }
        if (String.valueOf(txtPassword.getPassword()).isEmpty() ||
            String.valueOf(txtPassword.getPassword()).equals("Enter password")) {
            JOptionPane.showMessageDialog(this, "Password is required"); return; }
        if (phone.isEmpty() || phone.equals("Enter mobile number")) {
            JOptionPane.showMessageDialog(this, "Phone number is required"); return; }
        if (!male.isSelected() && !female.isSelected() && !other.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select gender"); return; }
        if (!java.isSelected() && !python.isSelected()
                && !webDev.isSelected() && !ai.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select at least one skill"); return; }
        if (txtAddress.getText().trim().isEmpty() ||
            txtAddress.getText().equals("Enter full address")) {
            JOptionPane.showMessageDialog(this, "Address is required"); return; }
        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(this, "Name should contain only alphabets"); return; }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(this, "Invalid Email Address"); return; }
        if (!phone.matches("[0-9]{10}")) {
            JOptionPane.showMessageDialog(this,
                "Mobile Number must contain exactly 10 digits"); return; }

        String gender = male.isSelected() ? "Male" :
                        female.isSelected() ? "Female" : "Other";

        String skills = "";
        if (java.isSelected())   skills += "Java ";
        if (python.isSelected()) skills += "Python ";
        if (webDev.isSelected()) skills += "Web Development ";
        if (ai.isSelected())     skills += "AI / ML ";

        String country = countryBox.getSelectedItem().toString();
        int    age     = (Integer) ageSpinner.getValue();
        String address = txtAddress.getText();
        String bio     = bioArea.getText();

        try {
            pst = con.prepareStatement(
                "SELECT * FROM students WHERE email=? AND id!=?");
            pst.setString(1, email); pst.setInt(2, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already exists"); return; }

            pst = con.prepareStatement(
                "UPDATE students SET " +
                "name=?,email=?,password=?,phone=?,gender=?," +
                "skills=?,country=?,age=?,address=?,bio=? " +
                "WHERE id=?");
            pst.setString(1, name);   pst.setString(2, email);
            pst.setString(3, String.valueOf(txtPassword.getPassword()));
            pst.setString(4, phone);  pst.setString(5, gender);
            pst.setString(6, skills); pst.setString(7, country);
            pst.setInt(8, age);       pst.setString(9, address);
            pst.setString(10, bio);   pst.setInt(11, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Record Updated Successfully");
            loadTable();
            clearForm();

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ===== DELETE =====
    public void deleteRecord() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select Row First"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this record?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        try {
            pst = con.prepareStatement("DELETE FROM students WHERE id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record Deleted Successfully");
            loadTable();
            clearForm();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ===== CLEAR =====
    public void clearForm() {
        txtName.setText(""); txtEmail.setText("");
        txtPassword.setText(""); txtPhone.setText("");
        txtAddress.setText(""); bioArea.setText("");

        addPlaceholder(txtName,     "Enter full name");
        addPlaceholder(txtEmail,    "Enter email address");
        addPlaceholder(txtPassword, "Enter password");
        addPlaceholder(txtPhone,    "Enter mobile number");
        addPlaceholder(txtAddress,  "Enter full address");
        addPlaceholder(bioArea,     "Write something about yourself");

        genderGroup.clearSelection();
        java.setSelected(false); python.setSelected(false);
        webDev.setSelected(false); ai.setSelected(false);
        countryBox.setSelectedIndex(0);
        ageSpinner.setValue(18);
        selectedRow = -1;
    }

    // ===== PLACEHOLDER =====
    public void addPlaceholder(JTextComponent field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText(""); field.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY); field.setText(placeholder); }
            }
        });
    }

    public void enableDynamicTooltip(JTextComponent field) {
        field.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                field.setToolTipText(field.getText()); }
        });
    }

    // ===== DB CONNECT =====
    public void connectDB() {
        con = DBConnection.getConnection();
        if (con != null)
            JOptionPane.showMessageDialog(this, "Database Connected Successfully");
    }

    // ===== LOAD TABLE =====
    public void loadTable() {
        try {
            pst = con.prepareStatement("SELECT * FROM students");
            rs  = pst.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),       rs.getString("name"),
                    rs.getString("email"), rs.getString("phone"),
                    rs.getString("gender"),rs.getString("skills"),
                    rs.getString("country"),rs.getInt("age"),
                    rs.getString("address"),rs.getString("bio")
                });
            }
            adjustTableColumns();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void adjustTableColumns() {
        for (int col = 0; col < table.getColumnCount(); col++) {
            if (col == 8 || col == 9) continue;
            int width = 50;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer r = table.getCellRenderer(row, col);
                Component c = table.prepareRenderer(r, row, col);
                width = Math.max(width, c.getPreferredSize().width + 20);
            }
            width = Math.max(70, Math.min(width, 250));
            table.getColumnModel().getColumn(col).setPreferredWidth(width);
        }
        table.getColumnModel().getColumn(8).setPreferredWidth(250);
        table.getColumnModel().getColumn(9).setPreferredWidth(250);
    }

    public String capitalizeWords(String text) {
        String[] words = text.trim().split("\\s+");
        String result = "";
        for (String word : words)
            result += Character.toUpperCase(word.charAt(0))
                    + word.substring(1).toLowerCase() + " ";
        return result.trim();
    }
}