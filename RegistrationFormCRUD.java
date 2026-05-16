import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;

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

    public RegistrationFormCRUD() {

        setTitle("Registration Form - CRUD Operations");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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

        table = new JTable(model);

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
            .getColumn(0)
            .setPreferredWidth(180);

        // Phone Column Width
        table.getColumnModel()
            .getColumn(2)
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
            .getColumn(6)
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

                txtName.setText(model.getValueAt(selectedRow, 0).toString());
                txtEmail.setText(model.getValueAt(selectedRow, 1).toString());
                txtPhone.setText(model.getValueAt(selectedRow, 2).toString());

                String gender = model.getValueAt(selectedRow, 3).toString();

                if (gender.equals("Male")) {
                    male.setSelected(true);
                } else if (gender.equals("Female")) {
                    female.setSelected(true);
                } else {
                    other.setSelected(true);
                }

                String skills = model.getValueAt(selectedRow, 4).toString();

                java.setSelected(skills.contains("Java"));
                python.setSelected(skills.contains("Python"));
                webDev.setSelected(skills.contains("Web Dev"));
                ai.setSelected(skills.contains("AI/ML"));

                countryBox.setSelectedItem(model.getValueAt(selectedRow, 5).toString());

                ageSpinner.setValue(
                        Integer.parseInt(model.getValueAt(selectedRow, 6).toString())
                );

                txtAddress.setText(model.getValueAt(selectedRow, 7).toString());

                bioArea.setText(model.getValueAt(selectedRow, 8).toString());
            }
        });

        add(panel);
        setVisible(true);
    }

    // ===== ADD RECORD =====
    public void addRecord() {

        String name = txtName.getText().trim();
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
            skills += "Web Dev ";
        }

        if (ai.isSelected()) {
            skills += "AI/ML ";
        }

        String country = countryBox.getSelectedItem().toString();

        int age = (Integer) ageSpinner.getValue();

        String address = txtAddress.getText();

        String bio = bioArea.getText();

        model.addRow(new Object[]{
                name,
                email,
                phone,
                gender,
                skills,
                country,
                age,
                address,
                bio
        });

        JOptionPane.showMessageDialog(this, "Record Added Successfully");

        clearForm();
    }

    // ===== UPDATE RECORD =====
    public void updateRecord() {

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select Row First");
            return;
        }

        model.setValueAt(txtName.getText(), selectedRow, 0);
        model.setValueAt(txtEmail.getText(), selectedRow, 1);
        model.setValueAt(txtPhone.getText(), selectedRow, 2);

        String gender = "";

        if (male.isSelected()) {
            gender = "Male";
        } else if (female.isSelected()) {
            gender = "Female";
        } else {
            gender = "Other";
        }

        model.setValueAt(gender, selectedRow, 3);

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

        model.setValueAt(skills, selectedRow, 4);

        model.setValueAt(countryBox.getSelectedItem(), selectedRow, 5);

        model.setValueAt(ageSpinner.getValue(), selectedRow, 6);

        model.setValueAt(txtAddress.getText(), selectedRow, 7);

        model.setValueAt(bioArea.getText(), selectedRow, 8);

        JOptionPane.showMessageDialog(this, "Record Updated Successfully");

        clearForm();
    }

    // ===== DELETE RECORD =====
    public void deleteRecord() {

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select Row First");
            return;
        }

        model.removeRow(selectedRow);

        JOptionPane.showMessageDialog(this, "Record Deleted Successfully");

        clearForm();
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
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new RegistrationFormCRUD();
        });
    }
}