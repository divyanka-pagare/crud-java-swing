package src.forms.transaction;

import src.db.DBConnection;
import src.models.Student;
import src.components.ModernButton;
import src.components.ModernComboBox;
import src.components.ModernPanel;
import src.components.ModernRadioButton;
import src.components.ModernTextField;
import src.constants.UIConstants;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.format.DateTimeFormatter;

import java.util.List;

import src.utils.TableUtils;
import src.utils.UIUtils;

import src.services.PaymentService;
import src.services.StudentService;

import src.repositories.PaymentRepository;
import src.repositories.EnrollmentRepository;

import src.utils.FormResetUtils;
import src.utils.PDFReceiptGenerator;

public class FeesReceiptForm extends JFrame {
   
    // ===== LEFT PANEL =====
    JComboBox<Student> studentDropdown;
    JLabel lblEmail, lblPhone, lblGender;
    JLabel lblCourseFees, lblDiscount, lblNetFees;
    
    JTable courseTable;
    DefaultTableModel courseTableModel;

    JRadioButton rbCash, rbCard, rbOnline;
    ButtonGroup  paymentGroup;

    JLabel  lblTransactionId;
    JTextField txtTransactionId;

    JButton payBtn, clearBtn, downloadBtn, backBtn, cancelEnrollBtn, btnRefresh, b;

    // ===== RIGHT PANEL =====
    JTable            receiptTable;
    DefaultTableModel receiptTableModel;
    JComboBox<String> filterDropdown;

    // ===== DATA =====
    private JPanel main;

    Connection        con;
    StudentService studentService;
    PaymentService paymentService;

    static final int    DISCOUNT_THRESHOLD = 3;
    static final double DISCOUNT_PERCENT   = 10.0;

    private PaymentRepository paymentRepository;
    private EnrollmentRepository enrollmentRepository;

    List<Integer> filteredCourseIds = null; // null means show all

    // called from Main menu — shows all unpaid courses
    public FeesReceiptForm() {
        this(null);

        setTitle("Course Selection Form");
        setSize(1200, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // called from CourseSelectionForm — shows only selected new courses
    public FeesReceiptForm(List<Integer> courseIds) {
        this.filteredCourseIds = courseIds;

        initializeFrame();

        initializeMainPanel();

        initializeLeftPanel();

        initializeRightPanel();

        initializeListeners();
        
        loadInitialData();

        setVisible(true);
    }
    
    private void initializeFrame() {

        setTitle("Fees Receipt");
    
        setSize(1220, 750);
    
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
        setLocationRelativeTo(null);
    
        con = DBConnection.getConnection();

        paymentService = new PaymentService(con);
        studentService = new StudentService(con);

        paymentRepository = new PaymentRepository(con);
        enrollmentRepository = new EnrollmentRepository(con);
    }

    private void initializeMainPanel() {

        main = new JPanel(null);
    
        main.setBackground(new Color(245, 247, 250));
    
        add(main);
    }
    
    private void initializeLeftPanel() {
        // ===== TITLE =====
        JLabel title = new JLabel("Fees Receipt & Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(30, 12, 500, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  LEFT SIDE
        // ─────────────────────────────────────────

        // --- Student Dropdown ---
        JLabel lStu = UIUtils.bold("Select Student:", 14);
        lStu.setBounds(30, 65, 150, 28);
        main.add(lStu);

        studentDropdown = new JComboBox<>();
        studentDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentDropdown.setBounds(185, 65, 270, 32);
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
        infoCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)),
            "Student Info",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        infoCard.setBounds(30, 108, 430, 85);
        main.add(infoCard);

        lblEmail  = info("Email  : —");
        lblPhone  = info("Phone  : —");
        lblGender = info("Gender : —");
        lblEmail .setBounds(10, 22, 410, 18);
        lblPhone .setBounds(10, 42, 410, 18);
        lblGender.setBounds(10, 62, 410, 18);
        infoCard.add(lblEmail);
        infoCard.add(lblPhone);
        infoCard.add(lblGender);

        // --- Enrolled Courses Table ---
        JLabel lCourses = bold("Enrolled Courses:", 13);
        lCourses.setBounds(30, 202, 200, 25);
        main.add(lCourses);

        String[] cCols = {"Course", "Duration", "Fees (₹)"};
        courseTableModel = new DefaultTableModel(cCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        courseTable = TableUtils.createStyledTable(courseTableModel);

        TableUtils.styleTable(courseTable);

        ((javax.swing.table.DefaultTableCellRenderer)
        courseTable.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(JLabel.CENTER);

        javax.swing.table.DefaultTableCellRenderer center =
                new javax.swing.table.DefaultTableCellRenderer();

        center.setHorizontalAlignment(JLabel.CENTER);

        TableUtils.styleTable(courseTable);

        JScrollPane courseScroll = new JScrollPane(courseTable);
        courseScroll.setBounds(30, 230, 430, 150);
        courseScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));
        main.add(courseScroll);

        // --- Fee Summary ---
        JPanel feeCard = new JPanel(null);
        feeCard.setBackground(Color.WHITE);
        feeCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)),
            "Fee Summary",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        feeCard.setBounds(30, 390, 430, 95);
        main.add(feeCard);

        JLabel lOrig = plain("Course Total:", 12);
        lOrig.setBounds(10, 22, 160, 20); feeCard.add(lOrig);
        lblCourseFees = plain("₹ 0.00", 12);
        lblCourseFees.setBounds(200, 22, 200, 20); feeCard.add(lblCourseFees);

        JLabel lDisc = plain("Discount (10%):", 12);
        lDisc.setBounds(10, 44, 160, 20); feeCard.add(lDisc);
        lblDiscount = plain("—", 12);
        lblDiscount.setForeground(new Color(40, 167, 69));
        lblDiscount.setBounds(200, 44, 200, 20); feeCard.add(lblDiscount);

        JLabel lNet = bold("Amount Payable:", 13);
        lNet.setBounds(10, 66, 160, 22); feeCard.add(lNet);
        lblNetFees = bold("₹ 0.00", 15);
        lblNetFees.setForeground(new Color(0, 102, 204));
        lblNetFees.setBounds(200, 64, 200, 24); feeCard.add(lblNetFees);

        // --- Payment Mode ---
        JPanel payCard = new JPanel(null);
        payCard.setBackground(Color.WHITE);
        payCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)),
            "Payment Mode",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        payCard.setBounds(30, 495, 430, 95);
        main.add(payCard);

        rbCash   = new JRadioButton("Cash");
        rbCard   = new JRadioButton("Card");
        rbOnline = new JRadioButton("Online");

        rbCash  .setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbCard  .setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rbOnline.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        rbCash  .setBackground(Color.WHITE);
        rbCard  .setBackground(Color.WHITE);
        rbOnline.setBackground(Color.WHITE);

        rbCash  .setBounds(15,  28, 80,  28);
        rbCard  .setBounds(120, 28, 80,  28);
        rbOnline.setBounds(225, 28, 100, 28);

        paymentGroup = new ButtonGroup();
        paymentGroup.add(rbCash);
        paymentGroup.add(rbCard);
        paymentGroup.add(rbOnline);

        payCard.add(rbCash);
        payCard.add(rbCard);
        payCard.add(rbOnline);

        // Transaction ID (shown for Card/Online only)
        lblTransactionId = plain("Transaction ID:", 12);
        lblTransactionId.setBounds(15, 62, 120, 22);
        lblTransactionId.setVisible(false);
        payCard.add(lblTransactionId);

        txtTransactionId = new JTextField();
        txtTransactionId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTransactionId.setBounds(140, 60, 270, 26);
        txtTransactionId.setVisible(false);
        payCard.add(txtTransactionId);

        // show/hide transaction ID based on payment mode
        rbCard  .addActionListener(e -> {
            lblTransactionId.setVisible(true);
            txtTransactionId.setVisible(true);
        });
        rbOnline.addActionListener(e -> {
            lblTransactionId.setVisible(false);
            txtTransactionId.setVisible(false);
            txtTransactionId.setText("");
        });
        rbCash  .addActionListener(e -> {
            lblTransactionId.setVisible(false);
            txtTransactionId.setVisible(false);
            txtTransactionId.setText("");
        });

        // --- Buttons ---
        payBtn      = colorBtn("PAY NOW",           UIConstants.PRIMARY,   30, 602);
        downloadBtn = colorBtn("Download Receipt",  UIConstants.SUCCESS,  200, 602);
        clearBtn    = colorBtn("CLEAR",             UIConstants.SECONDARY,  370, 602);
        
        backBtn = colorBtn("← Course Selection", UIConstants.DARK, 30, 655);
        // ===== CANCEL SELECTED ENROLLMENT BUTTON =====
        cancelEnrollBtn = new JButton("Cancel Selected Enrollment");
        cancelEnrollBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelEnrollBtn.setForeground(new Color(180, 30, 30));
        cancelEnrollBtn.setBounds(500, 550, 230, 28);
        
        
        main.add(cancelEnrollBtn);

        main.add(backBtn);
        main.add(payBtn);
        main.add(downloadBtn);
        main.add(clearBtn);
    }
    
    private void initializeRightPanel() {
        // ─────────────────────────────────────────
        //  RIGHT SIDE — Payment History Table
        // ─────────────────────────────────────────
        JLabel lRight = bold("Payment History", 16);
        lRight.setBounds(500, 12, 300, 38);
        main.add(lRight);

        JLabel lFilter = plain("Filter by student:", 13);
        lFilter.setBounds(500, 58, 140, 28);
        main.add(lFilter);

        filterDropdown = new JComboBox<>();
        filterDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterDropdown.setBounds(645, 58, 220, 30);
        main.add(filterDropdown);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBounds(880, 58, 90, 30);
        main.add(btnRefresh);

        String[] rCols = {
            "ID", "Student", "Courses Paid", "Total Fees", "Discount",
            "Amount Paid", "Mode", "Status", "Paid On"
        };
        receiptTableModel = new DefaultTableModel(rCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        receiptTable = TableUtils.createStyledTable(receiptTableModel);

        JScrollPane tableScroll = new JScrollPane(
            receiptTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        tableScroll.setBounds(500, 98, 730, 450);
        tableScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        tableScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));

        main.add(tableScroll);
    }
    
    private void initializeListeners() {
        studentDropdown.addActionListener(e -> onStudentSelected());
        payBtn     .addActionListener(e -> payFees());
        downloadBtn.addActionListener(e -> downloadReceipt());
        clearBtn   .addActionListener(e -> clearForm());
        
        backBtn.addActionListener(e -> {
            UIUtils.openFullScreen(new CourseSelectionForm()); 
            dispose();
        });

        cancelEnrollBtn.addActionListener(e -> cancelEnrollment());

        btnRefresh.addActionListener(e -> {
            if (filterDropdown.getSelectedItem() == null) return;
            String f = filterDropdown.getSelectedItem().toString();
            loadReceiptTable(f.equals("All Students") ? null : f);
        });

        filterDropdown.addActionListener(e -> {
            if (filterDropdown.getSelectedItem() == null) return;
            String f = filterDropdown.getSelectedItem().toString();
            loadReceiptTable(f.equals("All Students") ? null : f);
        });
    }
    
    private void loadInitialData() {
        studentService.loadStudents(studentDropdown);
        loadReceiptTable(null);
        populateFilterDropdown();

        if (studentDropdown.getItemCount() > 0) {
            onStudentSelected();
        }
    }


    // ─────────────────────────────────────────
    //  ON STUDENT SELECTED
    // ─────────────────────────────────────────
    public void onStudentSelected() {
        Student s = (Student) studentDropdown.getSelectedItem();

        TableUtils.styleTable(courseTable);
        lblCourseFees.setText("₹ 0.00");
        lblDiscount  .setText("—");
        lblNetFees   .setText("₹ 0.00");

        if (s == null) {
            lblEmail .setText("Email  : —");
            lblPhone .setText("Phone  : —");
            lblGender.setText("Gender : —");
            return;
        }

        lblEmail .setText("Email  : " + s.getEmail());
        lblPhone .setText("Phone  : " + s.getPhone());
        lblGender.setText("Gender : " + s.getGender());

        // load enrolled courses into table
        double total = 0.0;
        int    count = 0;

        try {
            
            ResultSet rs =
                enrollmentRepository.getUnpaidCourses(
                    s,
                    filteredCourseIds
        );

            while (rs.next()) {
                double fee = rs.getDouble("fees");
                courseTableModel.addRow(new Object[]{
                    rs.getString("course_name"),
                    rs.getString("duration"),
                    String.format("₹ %,.2f", fee)
                });
                total += fee;
                count++;
            }

        } catch (Exception ex) { ex.printStackTrace(); }

        if (count == 0) {
            lblCourseFees.setText("₹ 0.00");
            lblDiscount  .setText("— (all courses paid or none enrolled)");
            lblNetFees   .setText("₹ 0.00");
            return;
        }

        double disc    = count >= DISCOUNT_THRESHOLD ? total * DISCOUNT_PERCENT / 100.0 : 0.0;
        double payable = total - disc;

        lblCourseFees.setText(String.format("₹ %,.2f", total));
        if (disc > 0) {
            lblDiscount.setText(String.format("- ₹ %,.2f  (10%% on %d courses)", disc, count));
            lblDiscount.setForeground(new Color(40, 167, 69));
        } else {
            lblDiscount.setText("— (enroll 3+ courses for 10% off)");
            lblDiscount.setForeground(new Color(120,120,120));
        }
        lblNetFees.setText(String.format("₹ %,.2f", payable));

        TableUtils.resizeColumnWidth(courseTable);
    }

    // ─────────────────────────────────────────
    //  PAY FEES
    // ─────────────────────────────────────────
    public void payFees() {

        Student s = (Student) studentDropdown.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student"); return; }

        if (courseTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No unpaid courses found for " + s.getName() + ".\n" +
                "Either not enrolled in any course or all courses already paid.");
            return;
        }

        if (!rbCash.isSelected() && !rbCard.isSelected()
                && !rbOnline.isSelected()) {
            JOptionPane.showMessageDialog(this,
                "Please select a payment mode (Cash / Card / Online)"); return; }

        String mode = rbCash.isSelected() ? "Cash" :
                      rbCard.isSelected() ? "Card" : "Online";

        if (rbCard.isSelected()
                && txtTransactionId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter the Transaction ID for Card payment"); return; }

        // ===== CALCULATE =====
        double total   = 0.0;
        int    count   = courseTableModel.getRowCount();

        for (int i = 0; i < count; i++) {
            String feeStr = courseTableModel.getValueAt(i, 2)
                .toString().replace("₹", "").replace(",", "").trim();
            total += Double.parseDouble(feeStr);
        }

        double disc    = count >= DISCOUNT_THRESHOLD ? total * DISCOUNT_PERCENT / 100.0 : 0.0;
        double payable = total - disc;

        // ===== CONFIRM (only for Cash/Card) =====
        if (!rbOnline.isSelected()) {

            int confirm = JOptionPane.showConfirmDialog(this,
                "Student  : " + s.getName() + "\n" +
                "Courses  : " + count + "\n" +
                "Total    : ₹" + String.format("%,.2f", total) +
                (disc > 0 ? "\nDiscount : - ₹" + String.format("%,.2f", disc) : "") +
                "\nPayable  : ₹" + String.format("%,.2f", payable) +
                "\nMode     : " + mode +
                (txtTransactionId.isVisible() ?
                    "\nTxn ID   : " + txtTransactionId.getText().trim() : "") +
                "\n\nConfirm payment?",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;
        }

        // ===== ONLINE → open UPI screen =====
        if (rbOnline.isSelected()) {
            new UpiPaymentScreen(this, s, total, disc,
                payable, count);
            return;
        }

        // ===== CASH / CARD → save directly =====
        try {
            paymentService.savePaymentToDB(
                s, 
                total,
                disc, 
                payable, 
                mode,
                count,
                filteredCourseIds,
                courseTableModel
            );

            loadReceiptTable(null);
            populateFilterDropdown();

            int choice = JOptionPane.showConfirmDialog(this,
                "Payment recorded successfully!\n\n" +
                "Do you want to download the fee receipt?",
                "Payment Successful",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                PDFReceiptGenerator.generateReceipt(this, s, total, disc, payable,
                    mode, txtTransactionId.getText().trim(), courseTableModel);
            }

        } catch (Exception ex) { ex.printStackTrace(); }
    }
    // ─────────────────────────────────────────
    //  DOWNLOAD RECEIPT BUTTON
    // ─────────────────────────────────────────
    public void downloadReceipt() {

        Student s = (Student) studentDropdown.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a student first"); return; }

        // fetch from DB
        try {
            ResultSet rs = paymentRepository.getLatestPaymentByStudentId(s.getId());

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                    "No payment found for " + s.getName() + ".\n" +
                    "Please complete payment first."); return; }

            double total   = rs.getDouble("total_fees");
            double disc    = rs.getDouble("discount_amt");
            double paid    = rs.getDouble("amount_paid");
            String mode    = rs.getString("payment_mode");
            // int    count   = courseTableModel.getRowCount();

            PDFReceiptGenerator.generateReceipt(
                this,
                s,
                total,
                disc,
                paid,
                mode,
                txtTransactionId.getText().trim(),
                courseTableModel
            );

        } catch (Exception ex) { ex.printStackTrace(); }
    }

   
    // ─────────────────────────────────────────
    //  LOAD RECEIPT TABLE
    // ─────────────────────────────────────────
    public void loadReceiptTable(String nameFilter) {
        receiptTableModel.setRowCount(0);
        try {

            ResultSet rs = paymentRepository.getAllReceipts(nameFilter);
            // String q =
            //     "SELECT fp.id, s.name, " +
            //     "GROUP_CONCAT(c.course_name ORDER BY c.course_name SEPARATOR ', ') AS courses, " +
            //     "fp.total_fees, fp.discount_amt, " +
            //     "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
            //     "FROM fee_payments fp " +
            //     "JOIN students s ON fp.student_id = s.id " +
            //     "LEFT JOIN fee_payment_courses fpc ON fpc.fee_payment_id = fp.id " +
            //     "LEFT JOIN courses c ON fpc.course_id = c.id ";

            // if (nameFilter != null && !nameFilter.isBlank())
            //     q += "WHERE s.name=? ";

            // q += "GROUP BY fp.id, s.name, fp.total_fees, fp.discount_amt, " +
            //      "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
            //      "ORDER BY fp.paid_at DESC";

            // PreparedStatement pst = con.prepareStatement(q);
            // if (nameFilter != null && !nameFilter.isBlank())
            //     pst.setString(1, nameFilter);

            // ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date  = ts != null
                    ? ts.toLocalDateTime().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                    : "—";

                String courses = rs.getString("courses");
                if (courses == null) courses = "—";

                receiptTableModel.addRow(new Object[]{
                    rs.getInt("fp.id"),
                    rs.getString("s.name"),
                    courses,                          // courses column
                    String.format("₹ %,.2f", rs.getDouble("total_fees")),
                    rs.getDouble("discount_amt") > 0
                        ? String.format("- ₹ %,.2f", rs.getDouble("discount_amt"))
                        : "—",
                    String.format("₹ %,.2f", rs.getDouble("amount_paid")),
                    rs.getString("payment_mode"),
                    rs.getString("payment_status"),
                    date
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        TableUtils.resizeColumnWidth(receiptTable);
    }

    // ─────────────────────────────────────────
    //  POPULATE FILTER DROPDOWN
    // ─────────────────────────────────────────
    public void populateFilterDropdown() {
        filterDropdown.removeAllItems();
        filterDropdown.addItem("All Students");
        try {

            ResultSet rs = paymentRepository.getStudentsWithPayments();

            // PreparedStatement pst = con.prepareStatement(
            //     "SELECT DISTINCT s.name FROM fee_payments fp " +
            //     "JOIN students s ON fp.student_id=s.id ORDER BY s.name");
            // ResultSet rs = pst.executeQuery();
            while (rs.next())
                filterDropdown.addItem(rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  CANCEL ENROLLMENT
    // ─────────────────────────────────────────
    public void cancelEnrollment() {

        int row = receiptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a row from the Payment History table to cancel");
            return;
        }

        String studentName = receiptTableModel.getValueAt(row, 1).toString();
        String courses     = receiptTableModel.getValueAt(row, 2).toString();
        String amountPaid  = receiptTableModel.getValueAt(row, 5).toString();
        String mode        = receiptTableModel.getValueAt(row, 6).toString();
        int    paymentId   = Integer.parseInt(
                receiptTableModel.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this payment record?\n\n" +
            "Student  : " + studentName + "\n" +
            "Courses  : " + courses     + "\n" +
            "Amount   : " + amountPaid  + "\n" +
            "Mode     : " + mode        + "\n\n" +
            "This will remove the payment entry only.\n" +
            "Enrollment records will remain unchanged.",
            "Confirm Cancel Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;


            
            // delete course-level payment records first (foreign key)
            // PreparedStatement pst = con.prepareStatement(
            //     "DELETE FROM fee_payment_courses WHERE fee_payment_id = ?");
            // pst.setInt(1, paymentId);
            // pst.executeUpdate();

            // // delete main payment record
            // pst = con.prepareStatement(
            //     "DELETE FROM fee_payments WHERE id = ?");
            // pst.setInt(1, paymentId);
            // pst.executeUpdate();

        //     JOptionPane.showMessageDialog(this,
        //         "Payment record cancelled successfully.\n" +
        //         "Enrollment records are unchanged.");

        //     loadReceiptTable(null);
        //     populateFilterDropdown();
        //     onStudentSelected();

        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     JOptionPane.showMessageDialog(this,
        //         "Error: " + ex.getMessage());
        // }

        try {

            paymentRepository.deletePayment(paymentId);
        
            JOptionPane.showMessageDialog(this,
                "Payment record cancelled successfully.\n" +
                "Enrollment records are unchanged.");
        
            loadReceiptTable(null);
            populateFilterDropdown();
            onStudentSelected();
        
        } catch (Exception ex) {
            ex.printStackTrace();
        
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage());
        }

    }
    
    // ─────────────────────────────────────────
    //  CLEAR FORM
    // ─────────────────────────────────────────
    public void clearForm() {

        FormResetUtils.resetFeesForm(
            studentDropdown,
            paymentGroup,
            txtTransactionId,
            lblTransactionId,
            courseTableModel,
            lblCourseFees,
            lblDiscount,
            lblNetFees
        );
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private JLabel bold(String t, int s) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, s)); return l; }

    private JLabel plain(String t, int s) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, s)); return l; }

    private JLabel info(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(80,80,90)); return l; }

    private JButton colorBtn(String text, Color color, int x, int y) {
        b = new ModernButton(text, color);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(color); 
        b.setForeground(Color.WHITE);
        b.setBounds(x, y, 155, 38); 
        
        return b; 
    }
   
}