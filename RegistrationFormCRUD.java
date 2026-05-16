import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // ===== Labels =====
        JLabel title = new JLabel("Student Registration Form");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBounds(420, 10, 400, 40);
        panel.add(title);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(30, 70, 120, 25);
        panel.add(nameLabel);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(30, 110, 120, 25);
        panel.add(emailLabel);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 150, 120, 25);
        panel.add(passLabel);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(30, 190, 120, 25);
        panel.add(phoneLabel);

        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(30, 230, 120, 25);
        panel.add(genderLabel);

        JLabel skillsLabel = new JLabel("Skills:");
        skillsLabel.setBounds(30, 270, 120, 25);
        panel.add(skillsLabel);

        JLabel countryLabel = new JLabel("Country:");
        countryLabel.setBounds(30, 310, 120, 25);
        panel.add(countryLabel);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setBounds(30, 350, 120, 25);
        panel.add(ageLabel);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(30, 390, 120, 25);
        panel.add(addressLabel);

        JLabel bioLabel = new JLabel("Bio:");
        bioLabel.setBounds(30, 460, 120, 25);
        panel.add(bioLabel);

        // ===== Inputs =====
        txtName = new JTextField();
        txtName.setBounds(150, 70, 250, 25);
        panel.add(txtName);

        txtEmail = new JTextField();
        txtEmail.setBounds(150, 110, 250, 25);
        panel.add(txtEmail);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 150, 250, 25);
        panel.add(txtPassword);

        txtPhone = new JTextField();
        txtPhone.setBounds(150, 190, 250, 25);
        panel.add(txtPhone);

        // ===== Gender =====
        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        other = new JRadioButton("Other");

        male.setBounds(150, 230, 70, 25);
        female.setBounds(230, 230, 80, 25);
        other.setBounds(320, 230, 80, 25);

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
        webDev = new JCheckBox("Web Dev");
        ai = new JCheckBox("AI/ML");

        java.setBounds(150, 270, 80, 25);
        python.setBounds(240, 270, 80, 25);
        webDev.setBounds(330, 270, 100, 25);
        ai.setBounds(440, 270, 80, 25);

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
        countryBox.setBounds(150, 310, 250, 25);
        panel.add(countryBox);

        // ===== Age Spinner =====
        ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 100, 1));
        ageSpinner.setBounds(150, 350, 250, 25);
        panel.add(ageSpinner);

        // ===== Address =====
        txtAddress = new JTextArea();

        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);

        JScrollPane addressScroll = new JScrollPane(txtAddress);

        addressScroll.setBounds(150, 390, 250, 25);
        panel.add(addressScroll);

        // ===== Bio =====
        bioArea = new JTextArea();
        JScrollPane bioScroll = new JScrollPane(bioArea);
        bioScroll.setBounds(150, 460, 250, 80);
        panel.add(bioScroll);

        // ===== Buttons =====
        addBtn = new JButton("ADD");
        updateBtn = new JButton("UPDATE");
        deleteBtn = new JButton("DELETE");
        clearBtn = new JButton("CLEAR");

        addBtn.setBounds(30, 580, 100, 35);
        updateBtn.setBounds(150, 580, 100, 35);
        deleteBtn.setBounds(270, 580, 100, 35);
        clearBtn.setBounds(390, 580, 100, 35);

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

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBounds(550, 70, 600, 500);

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

        String name = txtName.getText();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();

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

        genderGroup.clearSelection();

        java.setSelected(false);
        python.setSelected(false);
        webDev.setSelected(false);
        ai.setSelected(false);

        countryBox.setSelectedIndex(0);

        ageSpinner.setValue(18);

        selectedRow = -1;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new RegistrationFormCRUD();
        });
    }
}