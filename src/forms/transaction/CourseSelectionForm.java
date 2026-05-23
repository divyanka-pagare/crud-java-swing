package src.forms.transaction;

import src.db.DBConnection;
import src.models.Course;
import src.models.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CourseSelectionForm extends JFrame {

    // ===== LEFT PANEL COMPONENTS =====
    JComboBox<Student> studentDropdown;
    JLabel lblStudentEmail, lblStudentPhone,
           lblStudentGender, lblAlreadyEnrolled;

    JPanel  coursePanel;
    JLabel  lblOriginalFees, lblDiscount, lblTotalFees;

    JButton enrollBtn, clearBtn, printBtn, btnRefresh;

    // ===== RIGHT PANEL =====
    JTable            enrollmentTable;
    DefaultTableModel tableModel;
    JComboBox<String> filterDropdown;

    // ===== DATA =====
    List<JCheckBox> courseCheckboxes = new ArrayList<>();
    List<Course>    courseList       = new ArrayList<>();

    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    // ===== DISCOUNT RULE =====
    static final int    DISCOUNT_THRESHOLD   = 3;
    static final double DISCOUNT_PERCENT     = 10.0;

    public CourseSelectionForm() {

        setTitle("Course Selection Form");
        setSize(1150, 720);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        // ===== MAIN PANEL =====
        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 247, 250));

        // ===== PAGE TITLE =====
        JLabel title = new JLabel("Course Selection & Enrollment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(30, 12, 600, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  LEFT SIDE
        // ─────────────────────────────────────────

        // --- Student selection ---
        JLabel lStudent = bold("Select Student:", 14);
        lStudent.setBounds(30, 68, 160, 28);
        main.add(lStudent);

        studentDropdown = new JComboBox<>();
        studentDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentDropdown.setBounds(190, 68, 270, 32);
        main.add(studentDropdown);

        studentDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Select Student --");
                    setForeground(Color.GRAY);
                }
                return this;
            }
        });


        // --- Student Info Card ---
        JPanel infoCard = new JPanel(null);
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createLineBorder(new Color(210,215,220)));
        infoCard.setBounds(30, 110, 430, 110);
        main.add(infoCard);

        JLabel lInfoTitle = bold("Student Info", 13);
        lInfoTitle.setBounds(10, 8, 200, 22);
        infoCard.add(lInfoTitle);

        lblStudentEmail    = info("Email: —");
        lblStudentPhone    = info("Phone: —");
        lblStudentGender   = info("Gender: —");
        lblAlreadyEnrolled = info("Already enrolled: —");

        lblStudentEmail   .setBounds(10, 32, 410, 18);
        lblStudentPhone   .setBounds(10, 52, 410, 18);
        lblStudentGender  .setBounds(10, 72, 410, 18);
        lblAlreadyEnrolled.setBounds(10, 90, 410, 18);
        infoCard.add(lblStudentEmail);
        infoCard.add(lblStudentPhone);
        infoCard.add(lblStudentGender);
        infoCard.add(lblAlreadyEnrolled);

        // --- Courses ---
        JLabel lCourses = bold("Select Courses:", 14);
        lCourses.setBounds(30, 232, 160, 28);
        main.add(lCourses);

        JLabel lDiscountNote = new JLabel(
            "* Enroll 3+ courses and get 10% off total fees");
        lDiscountNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lDiscountNote.setForeground(new Color(40, 167, 69));
        lDiscountNote.setBounds(190, 232, 300, 28);
        main.add(lDiscountNote);

        coursePanel = new JPanel();
        coursePanel.setLayout(new BoxLayout(coursePanel, BoxLayout.Y_AXIS));
        coursePanel.setBackground(Color.WHITE);

        JScrollPane courseScroll = new JScrollPane(coursePanel);
        courseScroll.setBounds(30, 265, 430, 185);
        courseScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        courseScroll.setBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)));
        main.add(courseScroll);

        // --- Fee Summary Card ---
        JPanel feeCard = new JPanel(null);
        feeCard.setBackground(Color.WHITE);
        feeCard.setBorder(BorderFactory.createLineBorder(new Color(210,215,220)));
        feeCard.setBounds(30, 460, 430, 100);
        main.add(feeCard);

        JLabel lFeeTitle = bold("Fee Summary", 13);
        lFeeTitle.setBounds(10, 8, 200, 22);
        feeCard.add(lFeeTitle);

        JLabel lOrig = plain("Original Total:", 13);
        lOrig.setBounds(10, 36, 160, 20); feeCard.add(lOrig);
        lblOriginalFees = plain("₹ 0.00", 13);
        lblOriginalFees.setBounds(180, 36, 230, 20); feeCard.add(lblOriginalFees);

        JLabel lDisc = plain("Discount (10%):", 13);
        lDisc.setBounds(10, 56, 160, 20); feeCard.add(lDisc);
        lblDiscount = plain("—", 13);
        lblDiscount.setForeground(new Color(40, 167, 69));
        lblDiscount.setBounds(180, 56, 230, 20); feeCard.add(lblDiscount);

        JLabel lTotal = bold("Amount Payable:", 14);
        lTotal.setBounds(10, 76, 160, 22); feeCard.add(lTotal);
        lblTotalFees = bold("₹ 0.00", 16);
        lblTotalFees.setForeground(new Color(0, 102, 204));
        lblTotalFees.setBounds(180, 74, 230, 24); feeCard.add(lblTotalFees);

        // --- Buttons ---
        enrollBtn = colorBtn("ENROLL",       new Color(0, 120, 215),  30, 575);
        clearBtn  = colorBtn("CLEAR",        new Color(108,117,125), 200, 575);
        JButton backBtn = colorBtn("← Registration", new Color(52, 58, 64), 30, 625); // BACK TO REGISTRATION BUTTON
        JButton feesBtn = colorBtn("Pay Fees →", new Color(153, 0, 153), 500, 625);

        main.add(enrollBtn);
        main.add(clearBtn);
        main.add(backBtn);
        main.add(feesBtn);

        // ─────────────────────────────────────────
        //  RIGHT SIDE — Enrollment Table
        // ─────────────────────────────────────────
        JLabel lTableTitle = bold("Enrollment Records", 16);
        lTableTitle.setBounds(490, 12, 300, 38);
        main.add(lTableTitle);

        JLabel lFilter = plain("Filter by student:", 13);
        lFilter.setBounds(490, 58, 140, 28);
        main.add(lFilter);

        filterDropdown = new JComboBox<>();
        filterDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterDropdown.setBounds(635, 58, 220, 30);
        main.add(filterDropdown);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBounds(870, 58, 90, 30);
        main.add(btnRefresh);

        String[] cols = {
            "ID", "Student", "Course", "Fees (₹)", "Duration", "Enrolled On"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        enrollmentTable = new JTable(tableModel) {

            @Override
            public String getToolTipText(MouseEvent e) {
        
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
        
                if (row > -1 && col > -1) {
        
                    Object value = getValueAt(row, col);
        
                    return value != null ? value.toString() : null;
                }
        
                return null;
            }
        };
        
        enrollmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        enrollmentTable.getTableHeader()
                .setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        enrollmentTable.setRowHeight(35);
        
        enrollmentTable.setSelectionBackground(
                new Color(184, 207, 229));
        
        enrollmentTable.setGridColor(Color.LIGHT_GRAY);
        
        enrollmentTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF);
        
        // ===== COLUMN ALIGNMENTS =====
        
        // Center align fees
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();
        
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        enrollmentTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(centerRenderer);
        
        // Center align duration
        DefaultTableCellRenderer durationRenderer =
                new DefaultTableCellRenderer();
        
        durationRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        enrollmentTable.getColumnModel()
                .getColumn(4)
                .setCellRenderer(durationRenderer);
        
        // ===== SCROLL PANE =====
        
        JScrollPane tableScroll = new JScrollPane(enrollmentTable);
        
        tableScroll.setBounds(490, 98, 620, 400);
        
        tableScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        tableScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        main.add(tableScroll);

        // Delete selected enrollment button
        JButton btnDelete = new JButton("Cancel Selected Enrollment");
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDelete.setForeground(new Color(180, 30, 30));
        btnDelete.setBounds(500, 520, 230, 28);
        main.add(btnDelete);

        add(main);

        // ===== LOAD DATA =====
        loadStudents();
        loadCourses();
        loadEnrollmentTable(null);
        populateFilterDropdown();

        if (studentDropdown.getItemCount() > 0) {
            onStudentSelected();
        }

        // ===== LISTENERS =====
        studentDropdown.addActionListener(e -> onStudentSelected());

        enrollBtn.addActionListener(e -> enrollStudent());
        clearBtn .addActionListener(e -> clearForm());
        backBtn.addActionListener(e -> {new src.forms.transaction.RegistrationForm();});
        feesBtn.addActionListener(e -> new src.forms.transaction.FeesReceiptForm());

        btnRefresh.addActionListener(e -> {
            String filter = filterDropdown.getSelectedItem().toString();
            loadEnrollmentTable(filter.equals("All Students") ? null : filter);
        });

        filterDropdown.addActionListener(e -> {
            if (filterDropdown.getSelectedItem() == null) return;
            String filter = filterDropdown.getSelectedItem().toString();
            loadEnrollmentTable(filter.equals("All Students") ? null : filter);
        });

        btnDelete.addActionListener(e -> cancelEnrollment());

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  LOAD STUDENTS
    // ─────────────────────────────────────────
    public void loadStudents() {
        studentDropdown.removeAllItems();

        studentDropdown.addItem(null); // default empty option
        try {
            pst = con.prepareStatement(
                "SELECT id,name,email,phone,gender,skills," +
                "country,age,address,bio FROM students ORDER BY name");
            rs = pst.executeQuery();
            while (rs.next()) {
                studentDropdown.addItem(new Student(
                    rs.getInt("id"),       rs.getString("name"),
                    rs.getString("email"), rs.getString("phone"),
                    rs.getString("gender"),rs.getString("skills"),
                    rs.getString("country"),rs.getInt("age"),
                    rs.getString("address"),rs.getString("bio")
                ));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  ON STUDENT SELECTED → update info panel
    // ─────────────────────────────────────────
    public void onStudentSelected() {

        Student s = (Student) studentDropdown.getSelectedItem();

        // reset fees
        lblOriginalFees.setText("₹ 0.00");
        lblDiscount.setText("—");
        lblTotalFees.setText("₹ 0.00");

        if (s == null) {
            lblStudentEmail .setText("Email:  —");
            lblStudentPhone .setText("Phone:  —");
            lblStudentGender.setText("Gender: —");
            lblAlreadyEnrolled.setText("Already enrolled: —");

            // show all courses when no student selected
            for (JCheckBox cb : courseCheckboxes) {
                cb.setVisible(true);
            }
            coursePanel.revalidate();
            coursePanel.repaint();
            return;
        }

        lblStudentEmail .setText("Email:  " + s.getEmail());
        lblStudentPhone .setText("Phone:  " + s.getPhone());
        lblStudentGender.setText("Gender: " + s.getGender());

        // ===== FETCH ALREADY ENROLLED COURSE IDs =====
        List<Integer> enrolledCourseIds = new ArrayList<>();
        List<String>  enrolledNames     = new ArrayList<>();

        try {
            pst = con.prepareStatement(
                "SELECT c.id, c.course_name " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.id " +
                "WHERE e.student_id = ?");
            pst.setInt(1, s.getId());
            rs = pst.executeQuery();

            while (rs.next()) {
                enrolledCourseIds.add(rs.getInt("id"));
                enrolledNames.add(rs.getString("course_name"));
            }

        } catch (Exception ex) { ex.printStackTrace(); }

        // ===== UPDATE ALREADY ENROLLED LABEL =====
        if (enrolledNames.isEmpty()) {
            lblAlreadyEnrolled.setText("Already enrolled: none");
        } else {
            lblAlreadyEnrolled.setText(
                "Already enrolled: " + String.join(", ", enrolledNames));
        }

        // ===== HIDE ALREADY ENROLLED COURSES, SHOW ONLY AVAILABLE =====
        for (int i = 0; i < courseCheckboxes.size(); i++) {
            Course    c  = courseList.get(i);
            JCheckBox cb = courseCheckboxes.get(i);

            if (enrolledCourseIds.contains(c.getId())) {
                // already enrolled — hide this checkbox
                cb.setSelected(false);
                cb.setVisible(false);
            } else {
                // not yet enrolled — show it
                cb.setVisible(true);
            }
        }

        coursePanel.revalidate();
        coursePanel.repaint();

        // recalculate fees after visibility change
        calculateFees();
    }

    // ─────────────────────────────────────────
    //  LOAD COURSE CHECKBOXES
    // ─────────────────────────────────────────
    public void loadCourses() {
        coursePanel.removeAll();
        courseCheckboxes.clear();
        courseList.clear();

        try {
            pst = con.prepareStatement(
                "SELECT id,course_name,fees,duration " +
                "FROM courses ORDER BY id");
            rs = pst.executeQuery();

            while (rs.next()) {
                Course c = new Course(
                    rs.getInt("id"), rs.getString("course_name"),
                    rs.getDouble("fees"), rs.getString("duration"));
                courseList.add(c);

                String label = String.format(
                    "%-30s  ₹ %,.2f   |   %s",
                    c.getCourseName(), c.getFees(), c.getDuration());

                JCheckBox cb = new JCheckBox(label);
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                cb.setBackground(Color.WHITE);
                cb.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                cb.addItemListener(e -> calculateFees());

                courseCheckboxes.add(cb);
                coursePanel.add(cb);
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        coursePanel.revalidate();
        coursePanel.repaint();
    }

    // ─────────────────────────────────────────
    //  CALCULATE FEES WITH DISCOUNT
    // ─────────────────────────────────────────
    public void calculateFees() {
        double original = 0.0;
        int    count    = 0;

        for (int i = 0; i < courseCheckboxes.size(); i++) {
            if (courseCheckboxes.get(i).isSelected()) {
                original += courseList.get(i).getFees();
                count++;
            }
        }

        lblOriginalFees.setText(String.format("₹ %,.2f", original));

        if (count >= DISCOUNT_THRESHOLD) {
            double discountAmt = original * DISCOUNT_PERCENT / 100.0;
            double payable     = original - discountAmt;
            lblDiscount .setText(String.format("- ₹ %,.2f  (%d courses)", discountAmt, count));
            lblTotalFees.setText(String.format("₹ %,.2f", payable));
        } else {
            lblDiscount .setText("— (select " + DISCOUNT_THRESHOLD + "+ for 10% off)");
            lblDiscount .setForeground(new Color(120,120,120));
            lblTotalFees.setText(String.format("₹ %,.2f", original));
        }
    }

    // ─────────────────────────────────────────
    //  ENROLL
    // ─────────────────────────────────────────
    public void enrollStudent() {
        Student s = (Student) studentDropdown.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student"); return; }
    
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < courseCheckboxes.size(); i++)
            if (courseCheckboxes.get(i).isSelected()) selected.add(i);
    
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one course"); return; }
    
        // ===== SAVE SELECTED COURSES BEFORE onStudentSelected() CLEARS THEM =====
        List<Course> enrolledCourses = new ArrayList<>();
    
        try {
            int enrolled = 0, skipped = 0;
    
            for (int idx : selected) {
                Course c = courseList.get(idx);
    
                pst = con.prepareStatement(
                    "SELECT id FROM enrollments " +
                    "WHERE student_id=? AND course_id=?");
                pst.setInt(1, s.getId()); pst.setInt(2, c.getId());
                rs = pst.executeQuery();
    
                if (rs.next()) { skipped++; continue; }
    
                pst = con.prepareStatement(
                    "INSERT INTO enrollments (student_id,course_id) VALUES (?,?)");
                pst.setInt(1, s.getId()); pst.setInt(2, c.getId());
                pst.executeUpdate();
                enrolled++;
                enrolledCourses.add(c); // save newly enrolled courses
            }
    
            if (enrolled == 0) {
                JOptionPane.showMessageDialog(this,
                    "All selected courses are already enrolled.\n" +
                    "No new enrollment was made.");
                return;
            }
    
            String msg = enrolled + " course(s) enrolled successfully.";
            if (skipped > 0) msg += "\n" + skipped + " skipped (already enrolled).";
    
            loadEnrollmentTable(null);
            populateFilterDropdown();
            onStudentSelected(); // this clears checkboxes — but we already saved courses above
    
            int choice = JOptionPane.showConfirmDialog(this,
                msg + "\n\n" +
                "Do you want to pay fees now?",
                "Enrollment Successful",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {

                List<Integer> newCourseIds = new ArrayList<>();
                for (int idx : selected) {
                    Course c = courseList.get(idx);
                    // only pass courses that were actually newly enrolled
                    // (not skipped ones)
                    newCourseIds.add(c.getId());
                }
                // open Fees Receipt Form directly for this student
                FeesReceiptForm feesForm = new FeesReceiptForm(newCourseIds);

                // auto-select the same student in fees form
                for (int i = 0; i < feesForm.studentDropdown.getItemCount(); i++) {
                    Object item = feesForm.studentDropdown.getItemAt(i);
                    if (item instanceof src.models.Student) {
                        src.models.Student st = (src.models.Student) item;
                        if (st.getId() == s.getId()) {
                            feesForm.studentDropdown.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
    
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    
    // ─────────────────────────────────────────
    //  PRINT RECEIPT
    // ─────────────────────────────────────────
    public void printReceipt() {
        Student s = (Student) studentDropdown.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this,
                "Select a student first"); return; }

        List<Course> selected = new ArrayList<>();
        for (int i = 0; i < courseCheckboxes.size(); i++)
            if (courseCheckboxes.get(i).isSelected()) selected.add(courseList.get(i));

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select at least one course to print receipt"); return; }

        double original = selected.stream()
            .mapToDouble(Course::getFees).sum();
        boolean hasDiscount = selected.size() >= DISCOUNT_THRESHOLD;
        double  discountAmt = hasDiscount ? original * DISCOUNT_PERCENT / 100.0 : 0;
        double  payable     = original - discountAmt;

        // Build receipt text
        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("       STUDENT ENROLLMENT RECEIPT\n");
        sb.append("============================================\n");
        sb.append(String.format("Date   : %s\n",
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
        sb.append(String.format("Student: %s\n", s.getName()));
        sb.append(String.format("Email  : %s\n", s.getEmail()));
        sb.append(String.format("Phone  : %s\n", s.getPhone()));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-28s  %10s\n", "Course", "Fees (₹)"));
        sb.append("--------------------------------------------\n");
        for (Course c : selected)
            sb.append(String.format("%-28s  %10.2f\n",
                c.getCourseName(), c.getFees()));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-28s  %10.2f\n", "Original Total", original));
        if (hasDiscount)
            sb.append(String.format("%-28s  %10.2f\n",
                "Discount (10%)", -discountAmt));
        sb.append(String.format("%-28s  %10.2f\n", "Amount Payable", payable));
        sb.append("============================================\n");
        sb.append("   Thank you! Keep learning and growing.\n");
        sb.append("============================================\n");

        // Show in a scrollable dialog
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Courier New", Font.PLAIN, 13));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(460, 380));

        int opt = JOptionPane.showConfirmDialog(this, sp,
            "Fee Receipt — " + s.getName(),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        // If user clicks OK, send to system printer
        if (opt == JOptionPane.OK_OPTION) {
            try {
                ta.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Print error: " + ex.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────
    //  CANCEL ENROLLMENT (delete row)
    // ─────────────────────────────────────────
    public void cancelEnrollment() {
        int row = enrollmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Select a row to cancel"); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel this enrollment?", "Confirm",
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        try {
            pst = con.prepareStatement(
                "DELETE FROM enrollments WHERE id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Enrollment cancelled successfully");
            loadEnrollmentTable(null);
            populateFilterDropdown();
            onStudentSelected();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  LOAD ENROLLMENT TABLE
    // ─────────────────────────────────────────
    public void loadEnrollmentTable(String studentNameFilter) {
        tableModel.setRowCount(0);
        try {
            String query =
                "SELECT e.id, s.name, c.course_name, " +
                "c.fees, c.duration, e.enrolled_at " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id=s.id " +
                "JOIN courses  c ON e.course_id =c.id ";

            if (studentNameFilter != null && !studentNameFilter.isBlank())
                query += "WHERE s.name=? ";
            query += "ORDER BY s.name, c.course_name";

            pst = con.prepareStatement(query);
            if (studentNameFilter != null && !studentNameFilter.isBlank())
                pst.setString(1, studentNameFilter);

            rs = pst.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("enrolled_at");
                String date  = ts != null
                    ? ts.toLocalDateTime().format(
                        DateTimeFormatter.ofPattern(
                            "dd-MM-yyyy HH:mm"))
                    : "—";
                tableModel.addRow(new Object[]{
                    rs.getInt("e.id"),
                    rs.getString("s.name"),
                    rs.getString("c.course_name"),
                    String.format("₹ %,.2f", 
                        rs.getDouble("c.fees")),
                    rs.getString("c.duration"),
                    date
                });
            }

            adjustEnrollmentTableColumns();

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ===== DYNAMIC TABLE COLUMN SIZE =====
    public void adjustEnrollmentTableColumns() {

        for (int col = 0;
            col < enrollmentTable.getColumnCount();
            col++) {

            int width = 80;

            for (int row = 0;
                row < enrollmentTable.getRowCount();
                row++) {

                TableCellRenderer renderer =
                        enrollmentTable.getCellRenderer(row, col);

                Component comp =
                        enrollmentTable.prepareRenderer(
                                renderer, row, col);

                width = Math.max(
                        width,
                        comp.getPreferredSize().width + 20
                );
            }

            width = Math.max(80, Math.min(width, 300));

            enrollmentTable.getColumnModel()
                    .getColumn(col)
                    .setPreferredWidth(width);
        }

        // custom larger columns

        enrollmentTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(180);

        enrollmentTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(220);

        enrollmentTable.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(180);
    }

    // ─────────────────────────────────────────
    //  POPULATE FILTER DROPDOWN
    // ─────────────────────────────────────────
    public void populateFilterDropdown() {
        filterDropdown.removeAllItems();
        filterDropdown.addItem("All Students");
        try {
            pst = con.prepareStatement(
                "SELECT DISTINCT s.name FROM enrollments e " +
                "JOIN students s ON e.student_id=s.id " +
                "ORDER BY s.name");
            rs = pst.executeQuery();
            while (rs.next())
                filterDropdown.addItem(rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  CLEAR FORM
    // ─────────────────────────────────────────
    public void clearForm() {
        if (studentDropdown.getItemCount() > 0)
            studentDropdown.setSelectedIndex(0);
        for (JCheckBox cb : courseCheckboxes) cb.setSelected(false);
        lblOriginalFees.setText("₹ 0.00");
        lblDiscount    .setText("—");
        lblTotalFees   .setText("₹ 0.00");
    }

    // ─────────────────────────────────────────
    //  LABEL HELPERS
    // ─────────────────────────────────────────
    private JLabel bold(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, size)); return l;
    }
    private JLabel plain(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, size)); return l;
    }
    private JLabel info(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(80, 80, 90)); return l;
    }
    private JButton colorBtn(String text, Color bg, int x, int y) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBounds(x, y, 155, 38); return b;
    }

    
    // ===== DOWNLOAD Fees Receipt DIRECTLY WITH PASSED DATA (called after enroll) =====
    public void downloadReceiptDirect(Student s, List<Course> courses) {

        if (courses == null || courses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses to generate fees receipt for.");
            return;
        }

        StringBuilder courseLines = new StringBuilder();
        double totalFees  = 0.0;
        int    courseCount = courses.size();

        for (Course c : courses) {
            courseLines.append(c.getCourseName())
                    .append("|")
                    .append(c.getFees())
                    .append("|")
                    .append(c.getDuration())
                    .append("|")
                    .append(java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter
                            .ofPattern("dd-MM-yyyy HH:mm:ss")))
                    .append("\n");
            totalFees += c.getFees();
        }

        double discountAmt = courseCount >= 3 ? totalFees * 10.0 / 100.0 : 0.0;
        double payable     = totalFees - discountAmt;

        // ===== CHOOSE SAVE LOCATION =====
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Fees Receipt as PDF");
        fc.setSelectedFile(new java.io.File(
            s.getName().replace(" ", "_") + "_Fees_Receipt.pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().endsWith(".pdf"))
            pdfFile = new java.io.File(pdfFile.getAbsolutePath() + ".pdf");

        // ===== RENDER TO IMAGE =====
        try {
            int W = 595 * 2;
            int H = 842 * 2;

            java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(W, H,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
            g.scale(2.0, 2.0);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            int x = 40, y = 40;
            int lineH = 22;

            // ── Blue header bar ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(0, 0, 595, 65);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g.drawString("STUDENT ENROLLMENT FEES RECEIPT", x, 42);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("Student Management System", x, 58);
            y = 85;

            // ── Student details ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Student : " + s.getName(),   x, y); y += lineH;
            g.drawString("Email   : " + s.getEmail(),  x, y); y += lineH;
            g.drawString("Phone   : " + s.getPhone(),  x, y); y += lineH;
            g.drawString("Gender  : " + s.getGender(), x, y); y += lineH;
            g.drawString("Date    : " +
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter
                        .ofPattern("dd-MM-yyyy HH:mm:ss")),
                x, y); y += lineH + 8;

            // ── Divider ──
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 14;

            // ── Table header ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(x - 5, y - 14, 520, 22);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("Course Name",  x,       y);
            g.drawString("Duration",     x + 200, y);
            g.drawString("Fees (INR)",   x + 310, y);
            g.drawString("Enrolled On",  x + 390, y);
            y += lineH;

            // ── Course rows ──
            boolean alt = false;
            for (String line : courseLines.toString().split("\n")) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 4) continue;

                if (alt) {
                    g.setColor(new Color(245, 248, 255));
                    g.fillRect(x - 5, y - 14, 520, 20);
                }
                alt = !alt;

                g.setColor(new Color(50, 50, 50));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString(p[0], x,       y);
                g.drawString(p[2], x + 200, y);
                g.drawString(String.format("%.2f",
                    Double.parseDouble(p[1])), x + 310, y);
                g.drawString(p[3], x + 390, y);
                y += lineH;
            }

            // ── Divider ──
            y += 6;
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 16;

            // ── Fee summary ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(String.format("Original Total  :  INR %.2f", totalFees),
                        x, y); y += lineH;

            if (courseCount >= 3) {
                g.setColor(new Color(40, 167, 69));
                g.drawString(String.format("Discount (10%%) :  - INR %.2f", discountAmt),
                            x, y); y += lineH;
            }

            g.setColor(new Color(0, 102, 204));
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(String.format("Amount Payable :  INR %.2f", payable),
                        x, y); y += lineH + 16;

            // ── Footer ──
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 14;
            g.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g.setColor(new Color(140, 140, 140));
            g.drawString("Thank you for enrolling. Keep learning and growing!", x, y);

            g.dispose();

            java.io.FileOutputStream fos =
                new java.io.FileOutputStream(pdfFile);
            writePDF(fos, img, W, H);
            fos.close();

            JOptionPane.showMessageDialog(this,
                "Fees receipt saved!\n" + pdfFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ===== DOWNLOAD FEES RECEIPT AS PDF =====
    public void downloadReceipt() {

        Student s = (Student) studentDropdown.getSelectedItem();

        if (s == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a student first");
            return;
        }

        // ===== GET CURRENTLY SELECTED COURSES =====
        List<String> alreadyEnrolledCourses = new ArrayList<>();
        List<Course> selectedCourses = new ArrayList<>();

        for (int i = 0; i < courseCheckboxes.size(); i++) {

            if (courseCheckboxes.get(i).isSelected()) {

                Course c = courseList.get(i);

                try {
                    pst = con.prepareStatement(
                        "SELECT id FROM enrollments " +
                        "WHERE student_id=? AND course_id=?");

                    pst.setInt(1, s.getId());
                    pst.setInt(2, c.getId());

                    rs = pst.executeQuery();

                    if (rs.next()) {
                        alreadyEnrolledCourses.add(c.getCourseName());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                selectedCourses.add(c);
            }
        }

        // if nothing selected
        if (selectedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one course");
            return;
        }

        // Notify if already enrolled
        if (!alreadyEnrolledCourses.isEmpty()) {

            int choice = JOptionPane.showConfirmDialog(this,
                "Student is already enrolled in:\n\n" +
                String.join("\n", alreadyEnrolledCourses) +
                "\n\nDo you still want to continue downloading the Fees Receipt?",
                "Already Enrolled",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        StringBuilder courseLines = new StringBuilder();
        double totalFees  = 0.0;
        int    courseCount = selectedCourses.size();

        for (Course c : selectedCourses) {

            courseLines.append(c.getCourseName())
                       .append("|")
                       .append(c.getFees())
                       .append("|")
                       .append(c.getDuration())
                       .append("|")
                       .append(LocalDateTime.now().format(
                           DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                       .append("\n");
    
            totalFees += c.getFees();
        }

        double discountAmt = courseCount >= 3 ? totalFees * 10.0 / 100.0 : 0.0;
        double payable     = totalFees - discountAmt;

        // ===== CHOOSE SAVE LOCATION =====
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Fees Receipt as PDF");
        fc.setSelectedFile(new java.io.File(
            s.getName().replace(" ", "_") + "_Fees_Receipt.pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().endsWith(".pdf"))
            pdfFile = new java.io.File(pdfFile.getAbsolutePath() + ".pdf");

        // ===== RENDER TO IMAGE =====
        try {
            int W = 595 * 2; // A4 width  at 2x
            int H = 842 * 2; // A4 height at 2x

            java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(W, H,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                               RenderingHints.VALUE_RENDER_QUALITY);
            g.scale(2.0, 2.0);

            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            int x = 40, y = 40;
            int lineH = 22;

            // ── Blue header bar ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(0, 0, 595, 65);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g.drawString("STUDENT ENROLLMENT FEES RECEIPT", x, 42);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("Student Management System", x, 58);
            y = 85;

            // ── Student details ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Student : " + s.getName(),   x, y); y += lineH;
            g.drawString("Email   : " + s.getEmail(),  x, y); y += lineH;
            g.drawString("Phone   : " + s.getPhone(),  x, y); y += lineH;
            g.drawString("Gender  : " + s.getGender(), x, y); y += lineH;

            g.drawString("Date and Time    : " + 
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                x, y); y += lineH + 8;

            // ── Divider ──
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 14;

            // ── Table header ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(x - 5, y - 14, 520, 22);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("Course Name",  x,       y);
            g.drawString("Duration",     x + 200, y);
            g.drawString("Fees (INR)",   x + 310, y);
            y += lineH;

            // ── Course rows ──
            boolean alt = false;
            for (String line : courseLines.toString().split("\n")) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 4) continue;

                if (alt) {
                    g.setColor(new Color(245, 248, 255));
                    g.fillRect(x - 5, y - 14, 520, 20);
                }
                alt = !alt;

                g.setColor(new Color(50, 50, 50));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString(p[0], x,       y);
                g.drawString(p[2], x + 200, y);
                g.drawString(String.format("%.2f",
                    Double.parseDouble(p[1])), x + 310, y);
                y += lineH;
            }

            // ── Divider ──
            y += 6;
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 16;

            // ── Fee summary ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(String.format("Original Total  :  INR %.2f", totalFees),
                         x, y); y += lineH;

            if (courseCount >= 3) {
                g.setColor(new Color(40, 167, 69));
                g.drawString(String.format("Discount (10%%) :  - INR %.2f", discountAmt),
                             x, y); y += lineH;
            }

            g.setColor(new Color(0, 102, 204));
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(String.format("Amount Payable :  INR %.2f", payable),
                         x, y); y += lineH + 16;

            // ── Footer ──
            g.setColor(new Color(200, 200, 200));
            g.drawLine(x, y, 555, y); y += 14;
            g.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g.setColor(new Color(140, 140, 140));
            g.drawString("Thank you for enrolling. Keep learning and growing!", x, y);

            g.dispose();

            // ===== SAVE AS PDF =====
            java.io.FileOutputStream fos =
                new java.io.FileOutputStream(pdfFile);
            writePDF(fos, img, W, H);
            fos.close();

            JOptionPane.showMessageDialog(this,
                "Fees receipt saved!\n" + pdfFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
        }
    }

    // ===== WRITE IMAGE AS PDF =====
    private void writePDF(java.io.OutputStream out,
                          java.awt.image.BufferedImage img,
                          int W, int H) throws Exception {

        java.io.ByteArrayOutputStream jpegOut =
            new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "jpeg", jpegOut);
        byte[] imgBytes = jpegOut.toByteArray();

        String w = String.valueOf(W);
        String h = String.valueOf(H);

        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R " +
                      "/MediaBox [0 0 " + w + " " + h + "] " +
                      "/Contents 4 0 R /Resources " +
                      "<< /XObject << /Im1 5 0 R >> >> >>\nendobj\n";
        String obj4body = "q " + w + " 0 0 " + h + " 0 0 cm /Im1 Do Q\n";
        String obj4 = "4 0 obj\n<< /Length " + obj4body.length() +
                      " >>\nstream\n" + obj4body + "endstream\nendobj\n";
        String obj5h = "5 0 obj\n<< /Type /XObject /Subtype /Image " +
                       "/Width " + w + " /Height " + h +
                       " /ColorSpace /DeviceRGB /BitsPerComponent 8 " +
                       "/Filter /DCTDecode /Length " + imgBytes.length +
                       " >>\nstream\n";
        String obj5f = "\nendstream\nendobj\n";

        String header = "%PDF-1.4\n";
        int off1 = header.length();
        int off2 = off1 + obj1.length();
        int off3 = off2 + obj2.length();
        int off4 = off3 + obj3.length();
        int off5 = off4 + obj4.length();

        String xref =
            "xref\n0 6\n" +
            "0000000000 65535 f \n" +
            String.format("%010d 00000 n \n", off1) +
            String.format("%010d 00000 n \n", off2) +
            String.format("%010d 00000 n \n", off3) +
            String.format("%010d 00000 n \n", off4) +
            String.format("%010d 00000 n \n", off5);

        int startxref = off5 + obj5h.length() + imgBytes.length + obj5f.length();

        String trailer = "trailer\n<< /Size 6 /Root 1 0 R >>\n" +
                         "startxref\n" + startxref + "\n%%EOF\n";

        java.io.PrintStream ps =
            new java.io.PrintStream(out, true, "UTF-8");
        ps.print(header);
        ps.print(obj1); ps.print(obj2);
        ps.print(obj3); ps.print(obj4);
        ps.print(obj5h);
        out.write(imgBytes);
        ps.print(obj5f);
        ps.print(xref);
        ps.print(trailer);
        ps.flush();
    }
}