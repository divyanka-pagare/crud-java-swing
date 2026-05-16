import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationFormCRUD extends JFrame {

    JTextField txtName, txtEmail, txtPhone, txtSearch;
    JTextArea txtAddress;

    JPasswordField txtPassword;

    JRadioButton male, female, other;
    ButtonGroup genderGroup;

    JCheckBox java, python, webDev, ai;

    JComboBox<String> countryBox;

    JSpinner ageSpinner;

    JTextArea bioArea;

    JTable table;
    DefaultTableModel model;

    JButton addBtn, updateBtn, deleteBtn, clearBtn;

    int selectedRow = -1;

    // ===== JDBC VARIABLES =====
    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    public RegistrationFormCRUD() {

        setTitle("Registration Form - CRUD Operations");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        connectDB();

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 247, 250));
        panel.setLayout(null);

        // ===== Labels =====
        JLabel title = new JLabel("Student Registration Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(330, 10, 600, 50);
        panel.add(title);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(30, 70, 120, 25);
        panel.add(nameLabel);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 120, 120, 25);
        panel.add(emailLabel);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 170, 120, 25);
        panel.add(passLabel);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 220, 120, 25);
        panel.add(phoneLabel);

        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(30, 270, 120, 25);
        panel.add(genderLabel);

        JLabel skillsLabel = new JLabel("Skills:");
        skillsLabel.setBounds(30, 320, 120, 25);
        panel.add(skillsLabel);

        JLabel countryLabel = new JLabel("Country:");
        countryLabel.setBounds(30, 370, 120, 25);
        panel.add(countryLabel);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setBounds(30, 420, 120, 25);
        panel.add(ageLabel);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(30, 470, 120, 25);
        panel.add(addressLabel);

        JLabel bioLabel = new JLabel("Bio:");
        bioLabel.setBounds(30, 550, 120, 25);
        panel.add(bioLabel);

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
        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        other = new JRadioButton("Other");

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
        java = new JCheckBox("Java");
        python = new JCheckBox("Python");
        webDev = new JCheckBox("Web Development");
        ai = new JCheckBox("AI / ML");

        // Set Position
        java.setLocation(150, 320);
        python.setLocation(220, 320);
        webDev.setLocation(300, 320);
        ai.setLocation(450, 320);

        // Auto Size According to Text
        java.setSize(java.getPreferredSize());
        python.setSize(python.getPreferredSize());
        webDev.setSize(webDev.getPreferredSize());
        ai.setSize(ai.getPreferredSize());

        panel.add(java);
        panel.add(python);
        panel.add(webDev);
        panel.add(ai);

        java.setToolTipText("Java");
        python.setToolTipText("Python");
        webDev.setToolTipText("Web Development");
        ai.setToolTipText("AI / ML");

        // ===== Country =====
        String countries[] = {
                "India",
                "USA",
                "Canada",
                "Germany",
                "Australia"
        };

        countryBox = new JComboBox<>(countries);
        countryBox.setBounds(150, 370, 300, 35);
        panel.add(countryBox);

        // ===== Age Spinner =====
        ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 100, 1));
        ageSpinner.setBounds(150, 420, 80, 30);
        panel.add(ageSpinner);

        // ===== Address =====
        txtAddress = new JTextArea();

        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);

        JScrollPane addressScroll = new JScrollPane(
            txtAddress,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

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
        addBtn = new JButton("ADD");
        updateBtn = new JButton("UPDATE");
        deleteBtn = new JButton("DELETE");
        clearBtn = new JButton("CLEAR");

        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        addBtn.setFont(btnFont);
        updateBtn.setFont(btnFont);
        deleteBtn.setFont(btnFont);
        clearBtn.setFont(btnFont);

        addBtn.setBackground(new Color(0, 120, 215));
        addBtn.setForeground(Color.WHITE);

        updateBtn.setBackground(new Color(40, 167, 69));
        updateBtn.setForeground(Color.WHITE);

        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);

        clearBtn.setBackground(new Color(108, 117, 125));
        clearBtn.setForeground(Color.WHITE);

        addBtn.setBounds(30, 640, 120, 40);
        updateBtn.setBounds(170, 640, 120, 40);
        deleteBtn.setBounds(310, 640, 120, 40);
        clearBtn.setBounds(450, 640, 120, 40);

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(clearBtn);

        // ===== Table =====
        String columns[] = {
                "ID",
                "Name",
                "Email",
                "Phone",
                "Gender",
                "Skills",
                "Country",
                "Age",
                "Address",
                "Bio"
        };

        model = new DefaultTableModel(columns, 0);

        table = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {

                java.awt.Point point = e.getPoint();

                int row = rowAtPoint(point);
                int column = columnAtPoint(point);

                if (row > -1 && column > -1) {

                    Object value = getValueAt(row, column);

                    if (value != null) {
                        return value.toString();
                    }
                }
                return null;
            }
        };

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        table.setSelectionBackground(new Color(184, 207, 229));

        table.setGridColor(Color.LIGHT_GRAY);

        // ===== TABLE SETTINGS =====
        table.setRowHeight(35);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Name Column Width
        table.getColumnModel()
            .getColumn(1)
            .setPreferredWidth(180);

        // Phone Column Width
        table.getColumnModel()
            .getColumn(4)
            .setPreferredWidth(130);

        // Center Alignment for Phone
        DefaultTableCellRenderer centerRenderer =
        new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel()
            .getColumn(2)
            .setCellRenderer(centerRenderer);

        // Right Alignment for Age
        DefaultTableCellRenderer rightRenderer =
            new DefaultTableCellRenderer();

        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        table.getColumnModel()
            .getColumn(7)
            .setCellRenderer(rightRenderer);

        JScrollPane tableScroll = new JScrollPane(table);

        tableScroll.setBounds(550, 70, 600, 500);

        tableScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        tableScroll.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel.add(tableScroll);

        // ===== ADD =====
        addBtn.addActionListener(e -> addRecord());

        // ===== UPDATE =====
        updateBtn.addActionListener(e -> updateRecord());

        // ===== DELETE =====
        deleteBtn.addActionListener(e -> deleteRecord());

        // ===== CLEAR =====
        clearBtn.addActionListener(e -> clearForm());

        // ===== TABLE CLICK =====
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                selectedRow = table.getSelectedRow();

                txtName.setText(model.getValueAt(selectedRow, 1).toString());
                txtEmail.setText(model.getValueAt(selectedRow, 2).toString());
                txtPhone.setText(model.getValueAt(selectedRow, 3).toString());

                String gender = model.getValueAt(selectedRow, 4).toString();

                if (gender.equals("Male")) {
                    male.setSelected(true);
                } else if (gender.equals("Female")) {
                    female.setSelected(true);
                } else {
                    other.setSelected(true);
                }

                String skills = model.getValueAt(selectedRow, 5).toString();

                java.setSelected(skills.contains("Java"));
                python.setSelected(skills.contains("Python"));
                webDev.setSelected(skills.contains("Web Dev"));
                ai.setSelected(skills.contains("AI/ML"));

                countryBox.setSelectedItem(model.getValueAt(selectedRow, 6).toString());

                ageSpinner.setValue(
                        Integer.parseInt(model.getValueAt(selectedRow, 7).toString())
                );

                txtAddress.setText(model.getValueAt(selectedRow, 8).toString());

                bioArea.setText(model.getValueAt(selectedRow, 9).toString());
            }
        });

        add(panel);

        loadTable();

        setVisible(true);
    }

    // ===== ADD RECORD =====
    public void addRecord() {

        String name = 
                capitalizeWords(txtName.getText().trim());
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        // ===== NAME VALIDATION =====
        if (!name.matches("[a-zA-Z ]+")) {

            JOptionPane.showMessageDialog(this,
                "Name should contain only alphabets");
            return;
        }

        // ===== EMAIL VALIDATION =====

        if(email.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                "Email cannot be empty");

            return;
        }

        // No spaces allowed
        if(email.contains(" ")) {

            JOptionPane.showMessageDialog(this,
                "Email should not contain spaces");

            return;
        }

        // Professional Email Regex
        String emailRegex =
        "^[A-Za-z][A-Za-z0-9+_.-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if(!email.matches(emailRegex)) {

            JOptionPane.showMessageDialog(this,
                    "Invalid Email Address");

            return;
        }

        // Length Validation
        if(email.length() > 50) {

            JOptionPane.showMessageDialog(this,
                    "Email is too long");

            return;
        }

        // ===== MOBILE VALIDATION =====
        if (!phone.matches("[0-9]{10}")) {

            JOptionPane.showMessageDialog(this,
                    "Mobile Number must contain exactly 10 digits");

            return;
        }

        String gender = "";

        if (male.isSelected()) {
            gender = "Male";
        } else if (female.isSelected()) {
            gender = "Female";
        } else {
            gender = "Other";
        }

        String skills = "";

        if (java.isSelected()) {
            skills += "Java ";
        }

        if (python.isSelected()) {
            skills += "Python ";
        }

        if (webDev.isSelected()) {
            skills += "Web Dev ";
        }

        if (ai.isSelected()) {
            skills += "AI/ML ";
        }

        String country = countryBox.getSelectedItem().toString();

        int age = (Integer) ageSpinner.getValue();

        String address = txtAddress.getText();

        String bio = bioArea.getText();

        try {

            String query =
                    "INSERT INTO students " +
                    "(name,email,password,phone,gender,skills,country,age,address,bio) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
        
            pst = con.prepareStatement(query);
        
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, txtPassword.getText());
            pst.setString(4, phone);
            pst.setString(5, gender);
            pst.setString(6, skills);
            pst.setString(7, country);
            pst.setInt(8, age);
            pst.setString(9, address);
            pst.setString(10, bio);
        
            pst.executeUpdate();
        
            JOptionPane.showMessageDialog(this,
                    "Record Added Successfully");

                    loadTable();
        
        } catch (Exception e) {
        
            e.printStackTrace();
        }

        clearForm();
    }

    // ===== UPDATE RECORD =====
    public void updateRecord() {

        if (selectedRow == -1) {
    
            JOptionPane.showMessageDialog(this,
                    "Select Row First");
    
            return;
        }
    
        int id = Integer.parseInt(
                model.getValueAt(selectedRow, 0).toString()
        );
    
        String name =
                capitalizeWords(txtName.getText().trim());
    
        String email = txtEmail.getText().trim();
    
        String phone = txtPhone.getText().trim();
    
        // ===== NAME VALIDATION =====
        if (!name.matches("[a-zA-Z ]+")) {
    
            JOptionPane.showMessageDialog(this,
                    "Name should contain only alphabets");
    
            return;
        }
    
        // ===== EMAIL VALIDATION =====
        String emailRegex =
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    
        if (!email.matches(emailRegex)) {
    
            JOptionPane.showMessageDialog(this,
                    "Invalid Email Address");
    
            return;
        }
    
        // ===== MOBILE VALIDATION =====
        if (!phone.matches("[0-9]{10}")) {
    
            JOptionPane.showMessageDialog(this,
                    "Mobile Number must contain exactly 10 digits");
    
            return;
        }
    
        String gender = "";
    
        if (male.isSelected()) {
            gender = "Male";
        } else if (female.isSelected()) {
            gender = "Female";
        } else {
            gender = "Other";
        }
    
        String skills = "";
    
        if (java.isSelected()) {
            skills += "Java ";
        }
    
        if (python.isSelected()) {
            skills += "Python ";
        }
    
        if (webDev.isSelected()) {
            skills += "Web Development ";
        }
    
        if (ai.isSelected()) {
            skills += "AI / ML ";
        }
    
        String country =
                countryBox.getSelectedItem().toString();
    
        int age = (Integer) ageSpinner.getValue();
    
        String address = txtAddress.getText();
    
        String bio = bioArea.getText();
    
        try {
    
            String query =
                    "UPDATE students SET " +
                    "name=?, email=?, password=?, phone=?, gender=?, " +
                    "skills=?, country=?, age=?, address=?, bio=? " +
                    "WHERE id=?";
    
            pst = con.prepareStatement(query);
    
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3,
                    String.valueOf(txtPassword.getPassword()));
            pst.setString(4, phone);
            pst.setString(5, gender);
            pst.setString(6, skills);
            pst.setString(7, country);
            pst.setInt(8, age);
            pst.setString(9, address);
            pst.setString(10, bio);
    
            pst.setInt(11, id);
    
            pst.executeUpdate();
    
            JOptionPane.showMessageDialog(this,
                    "Record Updated Successfully");
    
            loadTable();
    
            clearForm();
    
        } catch (Exception e) {
    
            e.printStackTrace();
        }
    }
    // ===== DELETE RECORD =====
    public void deleteRecord() {

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
            "Select Row First");
            return;
        }

        int id = Integer.parseInt(
            model.getValueAt(selectedRow, 0).toString()
        );

        try {

            String query =
                    "DELETE FROM students WHERE id=?";

            pst = con.prepareStatement(query);

            pst.setInt(1, id);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Record Deleted Successfully");

            loadTable();

            clearForm();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ===== CLEAR FORM =====
    public void clearForm() {

        txtName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        bioArea.setText("");

        // Restore Placeholders
        addPlaceholder(txtName, "Enter full name");
        addPlaceholder(txtEmail, "Enter email address");
        addPlaceholder(txtPassword, "Enter password");
        addPlaceholder(txtPhone, "Enter mobile number");
        addPlaceholder(txtAddress, "Enter full address");
        addPlaceholder(bioArea, "Write something about yourself");

        genderGroup.clearSelection();

        java.setSelected(false);
        python.setSelected(false);
        webDev.setSelected(false);
        ai.setSelected(false);

        countryBox.setSelectedIndex(0);

        ageSpinner.setValue(18);

        selectedRow = -1;
    }

    // ===== PLACEHOLDER METHOD =====
    public void addPlaceholder(JTextComponent field, String placeholder) {

        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {

                if (field.getText().equals(placeholder)) {

                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

                if (field.getText().isEmpty()) {

                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    public void enableDynamicTooltip(JTextComponent field) {

        field.addKeyListener(new KeyAdapter() {
    
            @Override
            public void keyReleased(KeyEvent e) {
    
                field.setToolTipText(field.getText());
            }
        });
    }
    
    // ===== DATABASE CONNECTION =====
    public void connectDB() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/studentdb",
                    "root",
                "Ritesh@07"
            );

            JOptionPane.showMessageDialog(this,
                "Database Connected Successfully");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // ===== LOAD TABLE DATA =====
    public void loadTable() {

        try {

            pst = con.prepareStatement(
                    "SELECT * FROM students"
            );

            rs = pst.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("gender"),
                        rs.getString("skills"),
                        rs.getString("country"),
                        rs.getInt("age"),
                        rs.getString("address"),
                        rs.getString("bio")
                });
            }

            adjustTableColumns();

    
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void adjustTableColumns() {

        for (int column = 0; column < table.getColumnCount(); column++) {

            // Skip Address and Bio columns
            if (column == 8 || column == 9) {
                continue;
            }

            int width = 50;

            for (int row = 0; row < table.getRowCount(); row++) {

                TableCellRenderer renderer =
                    table.getCellRenderer(row, column);

                Component comp =
                    table.prepareRenderer(renderer, row, column);

                width = Math.max(width,
                    comp.getPreferredSize().width + 20);
            }

            // Minimum + Maximum Limits
            width = Math.max(width, 70);
            width = Math.min(width, 250);

            table.getColumnModel()
                    .getColumn(column)
                    .setPreferredWidth(width);
        }

        // Fixed Width for Address & Bio
        table.getColumnModel()
                .getColumn(8)
                .setPreferredWidth(250);

        table.getColumnModel()
                .getColumn(9)
                .setPreferredWidth(250);
    }

    public String capitalizeWords(String text) {

        String[] words = text.trim().split("\\s+");

        String result = "";

        for (String word : words) {

            result += Character.toUpperCase(word.charAt(0))
                    + word.substring(1).toLowerCase()
                    + " ";
        }

        return result.trim();
    }
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new RegistrationFormCRUD();
        });
    }
}