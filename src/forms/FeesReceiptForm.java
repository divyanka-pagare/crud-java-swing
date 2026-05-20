package src.forms;

import src.db.DBConnection;
import src.models.Student;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    JButton payBtn, clearBtn, downloadBtn;

    // ===== RIGHT PANEL =====
    JTable            receiptTable;
    DefaultTableModel receiptTableModel;
    JComboBox<String> filterDropdown;

    // ===== DATA =====
    Connection        con;
    PreparedStatement pst;
    ResultSet         rs;

    static final int    DISCOUNT_THRESHOLD = 3;
    static final double DISCOUNT_PERCENT   = 10.0;

    public FeesReceiptForm() {

        setTitle("Fees Receipt");
        setSize(1200, 750);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        con = DBConnection.getConnection();

        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 247, 250));

        // ===== TITLE =====
        JLabel title = new JLabel("Fees Receipt & Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(30, 12, 500, 42);
        main.add(title);

        // ─────────────────────────────────────────
        //  LEFT SIDE
        // ─────────────────────────────────────────

        // --- Student Dropdown ---
        JLabel lStu = bold("Select Student:", 14);
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
        courseTable = new JTable(courseTableModel) {

            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer,
                    int row, int column) {

                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0
                            ? Color.WHITE
                            : new Color(245, 247, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }

                return c;
            }
        };

        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        courseTable.setRowHeight(30);

        courseTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        courseTable.getTableHeader().setBackground(
                new Color(0, 102, 204));

        courseTable.getTableHeader().setForeground(Color.WHITE);

        courseTable.setGridColor(new Color(230,230,230));

        courseTable.setSelectionBackground(
                new Color(184, 207, 229));

        courseTable.setShowVerticalLines(false);

        courseTable.setIntercellSpacing(new Dimension(0, 0));

        ((javax.swing.table.DefaultTableCellRenderer)
        courseTable.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(JLabel.CENTER);

        javax.swing.table.DefaultTableCellRenderer center =
                new javax.swing.table.DefaultTableCellRenderer();

        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < courseTable.getColumnCount(); i++) {
            courseTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center);
    }

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
            lblTransactionId.setVisible(true);
            txtTransactionId.setVisible(true);
        });
        rbCash  .addActionListener(e -> {
            lblTransactionId.setVisible(false);
            txtTransactionId.setVisible(false);
            txtTransactionId.setText("");
        });

        // --- Buttons ---
        payBtn      = colorBtn("PAY NOW",           new Color(0, 120, 215),   30, 602);
        downloadBtn = colorBtn("Download Receipt",  new Color(40, 167, 69),  200, 602);
        clearBtn    = colorBtn("CLEAR",             new Color(108,117,125),  370, 602);

        main.add(payBtn);
        main.add(downloadBtn);
        main.add(clearBtn);

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

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBounds(880, 58, 90, 30);
        main.add(btnRefresh);

        String[] rCols = {
            "ID", "Student", "Total Fees", "Discount",
            "Amount Paid", "Mode", "Status", "Paid On"
        };
        receiptTableModel = new DefaultTableModel(rCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        receiptTable = new JTable(receiptTableModel) {

            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer,
                    int row, int column) {
        
                Component c = super.prepareRenderer(renderer, row, column);
        
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0
                            ? Color.WHITE
                            : new Color(245, 247, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
        
                return c;
            }
        };
        
        receiptTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        receiptTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        receiptTable.setRowHeight(32);
        
        receiptTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        
        receiptTable.getTableHeader().setBackground(
                new Color(0, 102, 204));
        
        receiptTable.getTableHeader().setForeground(Color.WHITE);
        
        receiptTable.setGridColor(new Color(230,230,230));
        
        receiptTable.setSelectionBackground(
                new Color(184, 207, 229));
        
        receiptTable.setShowVerticalLines(false);
        
        receiptTable.setIntercellSpacing(new Dimension(0, 0));
        
        ((javax.swing.table.DefaultTableCellRenderer)
        receiptTable.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(JLabel.CENTER);
        
        javax.swing.table.DefaultTableCellRenderer center2 =
                new javax.swing.table.DefaultTableCellRenderer();
        
        center2.setHorizontalAlignment(JLabel.CENTER);
        
        for (int i = 0; i < receiptTable.getColumnCount(); i++) {
            receiptTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center2);
        }

        JScrollPane tableScroll = new JScrollPane(
            receiptTable,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        tableScroll.setBounds(500, 98, 660, 450);
        tableScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        tableScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(5,5,5,5)
        ));

        main.add(tableScroll);

        add(main);

        // ===== LOAD DATA =====
        loadStudents();
        loadReceiptTable(null);
        populateFilterDropdown();

        if (studentDropdown.getItemCount() > 0) {
            onStudentSelected();
        }

        // ===== LISTENERS =====
        studentDropdown.addActionListener(e -> onStudentSelected());
        payBtn     .addActionListener(e -> payFees());
        downloadBtn.addActionListener(e -> downloadReceipt());
        clearBtn   .addActionListener(e -> clearForm());

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

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  LOAD STUDENTS
    // ─────────────────────────────────────────
    public void loadStudents() {
        studentDropdown.removeAllItems();
        studentDropdown.addItem(null);
        try {
            pst = con.prepareStatement(
                "SELECT id,name,email,phone,gender,skills," +
                "country,age,address,bio FROM students ORDER BY name");
            rs = pst.executeQuery();
            while (rs.next()) {
                studentDropdown.addItem(new Student(
                    rs.getInt("id"),        rs.getString("name"),
                    rs.getString("email"),  rs.getString("phone"),
                    rs.getString("gender"), rs.getString("skills"),
                    rs.getString("country"),rs.getInt("age"),
                    rs.getString("address"),rs.getString("bio")
                ));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  ON STUDENT SELECTED
    // ─────────────────────────────────────────
    public void onStudentSelected() {
        Student s = (Student) studentDropdown.getSelectedItem();

        courseTableModel.setRowCount(0);
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
            pst = con.prepareStatement(
                "SELECT c.course_name, c.duration, c.fees " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.id " +
                "WHERE e.student_id = ?");
            pst.setInt(1, s.getId());
            rs = pst.executeQuery();

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
            lblDiscount  .setText("— (no courses enrolled)");
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

        resizeColumnWidth(courseTable);
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
                "This student has no enrolled courses.\n" +
                "Please enroll in courses first."); return; }

        if (!rbCash.isSelected() && !rbCard.isSelected()
                && !rbOnline.isSelected()) {
            JOptionPane.showMessageDialog(this,
                "Please select a payment mode (Cash / Card / Online)"); return; }

        String mode = rbCash.isSelected() ? "Cash" :
                      rbCard.isSelected() ? "Card" : "Online";

        if ((rbCard.isSelected() || rbOnline.isSelected())
                && txtTransactionId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter the Transaction ID for " + mode + " payment"); return; }

        // ===== CHECK ALREADY PAID =====
        try {
            pst = con.prepareStatement(
                "SELECT id FROM fee_payments WHERE student_id=?");
            pst.setInt(1, s.getId());
            rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                    s.getName() + " has already paid fees.\n" +
                    "You can download the existing receipt.");
                return;
            }
        } catch (Exception ex) { ex.printStackTrace(); }

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

        // ===== CONFIRM =====
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

        // ===== SAVE TO DB =====
        try {
            pst = con.prepareStatement(
                "INSERT INTO fee_payments " +
                "(student_id, total_fees, discount_amt, amount_paid, " +
                "payment_mode, payment_status) " +
                "VALUES (?,?,?,?,?,'Paid')");
            pst.setInt(1, s.getId());
            pst.setDouble(2, total);
            pst.setDouble(3, disc);
            pst.setDouble(4, payable);
            pst.setString(5, mode);
            pst.executeUpdate();

            loadReceiptTable(null);
            populateFilterDropdown();

            int choice = JOptionPane.showConfirmDialog(this,
                "Payment recorded successfully!\n\n" +
                "Do you want to download the fee receipt?",
                "Payment Successful",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                downloadReceiptForStudent(s, total, disc, payable,
                    mode, txtTransactionId.getText().trim(), count);
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
            pst = con.prepareStatement(
                "SELECT * FROM fee_payments WHERE student_id=? " +
                "ORDER BY paid_at DESC LIMIT 1");
            pst.setInt(1, s.getId());
            rs = pst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                    "No payment found for " + s.getName() + ".\n" +
                    "Please complete payment first."); return; }

            double total   = rs.getDouble("total_fees");
            double disc    = rs.getDouble("discount_amt");
            double paid    = rs.getDouble("amount_paid");
            String mode    = rs.getString("payment_mode");
            int    count   = courseTableModel.getRowCount();

            downloadReceiptForStudent(s, total, disc, paid, mode, "", count);

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  GENERATE & SAVE PDF RECEIPT
    // ─────────────────────────────────────────
    public void downloadReceiptForStudent(Student s,
            double total, double disc, double payable,
            String mode, String txnId, int courseCount) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Fee Receipt as PDF");
        fc.setSelectedFile(new java.io.File(
            s.getName().replace(" ", "_") + "_FeeReceipt.pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File pdfFile = fc.getSelectedFile();
        if (!pdfFile.getName().endsWith(".pdf"))
            pdfFile = new java.io.File(pdfFile.getAbsolutePath() + ".pdf");

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

            // ── Header ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(0, 0, 595, 70);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g.drawString("FEES RECEIPT", x, 38);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("Student Management System", x, 55);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("Date: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                400, 55);
            y = 90;

            // ── Receipt No ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("Receipt No : REC-" + s.getId() + "-" +
                System.currentTimeMillis() % 10000, x, y); y += lineH;

            // ── Student Details ──
            g.drawString("Student    : " + s.getName(),   x, y); y += lineH;
            g.drawString("Email      : " + s.getEmail(),  x, y); y += lineH;
            g.drawString("Phone      : " + s.getPhone(),  x, y); y += lineH;
            g.drawString("Gender     : " + s.getGender(), x, y); y += lineH + 8;

            // ── Divider ──
            g.setColor(new Color(200,200,200));
            g.drawLine(x, y, 555, y); y += 14;

            // ── Course Table Header ──
            g.setColor(new Color(0, 102, 204));
            g.fillRect(x - 5, y - 14, 520, 22);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("Course Name", x,       y);
            g.drawString("Duration",    x + 250, y);
            g.drawString("Fees (INR)",  x + 390, y);
            y += lineH;

            // ── Course Rows ──
            boolean alt = false;
            for (int i = 0; i < courseTableModel.getRowCount(); i++) {
                String cName = courseTableModel.getValueAt(i, 0).toString();
                String cDur  = courseTableModel.getValueAt(i, 1).toString();
                String cFee  = courseTableModel.getValueAt(i, 2).toString()
                    .replace("₹","").replace(",","").trim();

                if (alt) {
                    g.setColor(new Color(245, 248, 255));
                    g.fillRect(x - 5, y - 14, 520, 20);
                }
                alt = !alt;

                g.setColor(new Color(50, 50, 50));
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g.drawString(cName, x,       y);
                g.drawString(cDur,  x + 250, y);
                g.drawString(String.format("%.2f",
                    Double.parseDouble(cFee)), x + 390, y);
                y += lineH;
            }

            // ── Divider ──
            y += 6;
            g.setColor(new Color(200,200,200));
            g.drawLine(x, y, 555, y); y += 16;

            // ── Fee Summary ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(String.format("Course Total    :  INR %.2f", total),
                         x, y); y += lineH;

            if (disc > 0) {
                g.setColor(new Color(40, 167, 69));
                g.drawString(String.format("Discount (10%%) :  - INR %.2f", disc),
                             x, y); y += lineH;
            }

            g.setColor(new Color(0, 102, 204));
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(String.format("Amount Paid     :  INR %.2f", payable),
                         x, y); y += lineH + 8;

            // ── Payment Details ──
            g.setColor(new Color(50, 50, 50));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Payment Mode    :  " + mode, x, y); y += lineH;

            if (!txnId.isEmpty()) {
                g.drawString("Transaction ID  :  " + txnId, x, y); y += lineH;
            }

            // ── Status Badge ──
            y += 6;
            g.setColor(new Color(40, 167, 69));
            g.fillRoundRect(x, y, 80, 24, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("PAID", x + 22, y + 16);
            y += 40;

            // ── Footer ──
            g.setColor(new Color(200,200,200));
            g.drawLine(x, y, 555, y); y += 14;
            g.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g.setColor(new Color(140,140,140));
            g.drawString("Thank you for your payment. Keep learning and growing!", x, y);

            g.dispose();

            java.io.FileOutputStream fos =
                new java.io.FileOutputStream(pdfFile);
            writePDF(fos, img, W, H);
            fos.close();

            JOptionPane.showMessageDialog(this,
                "Receipt saved!\n" + pdfFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────
    //  LOAD RECEIPT TABLE
    // ─────────────────────────────────────────
    public void loadReceiptTable(String nameFilter) {
        receiptTableModel.setRowCount(0);
        try {
            String q =
                "SELECT fp.id, s.name, fp.total_fees, fp.discount_amt, " +
                "fp.amount_paid, fp.payment_mode, fp.payment_status, fp.paid_at " +
                "FROM fee_payments fp " +
                "JOIN students s ON fp.student_id = s.id ";
            if (nameFilter != null && !nameFilter.isBlank())
                q += "WHERE s.name=? ";
            q += "ORDER BY fp.paid_at DESC";

            pst = con.prepareStatement(q);
            if (nameFilter != null && !nameFilter.isBlank())
                pst.setString(1, nameFilter);

            rs = pst.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("paid_at");
                String date  = ts != null
                    ? ts.toLocalDateTime().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                    : "—";
                receiptTableModel.addRow(new Object[]{
                    rs.getInt("fp.id"),
                    rs.getString("s.name"),
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

        resizeColumnWidth(receiptTable);
    }

    // ─────────────────────────────────────────
    //  POPULATE FILTER DROPDOWN
    // ─────────────────────────────────────────
    public void populateFilterDropdown() {
        filterDropdown.removeAllItems();
        filterDropdown.addItem("All Students");
        try {
            pst = con.prepareStatement(
                "SELECT DISTINCT s.name FROM fee_payments fp " +
                "JOIN students s ON fp.student_id=s.id ORDER BY s.name");
            rs = pst.executeQuery();
            while (rs.next())
                filterDropdown.addItem(rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ─────────────────────────────────────────
    //  CLEAR FORM
    // ─────────────────────────────────────────
    public void clearForm() {
        studentDropdown.setSelectedIndex(0);
        paymentGroup.clearSelection();
        txtTransactionId.setText("");
        txtTransactionId.setVisible(false);
        lblTransactionId.setVisible(false);
        courseTableModel.setRowCount(0);
        lblCourseFees.setText("₹ 0.00");
        lblDiscount  .setText("—");
        lblNetFees   .setText("₹ 0.00");
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

    private JButton colorBtn(String t, Color bg, int x, int y) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBounds(x, y, 155, 38); return b; }

    private void resizeColumnWidth(JTable table) {

        for (int column = 0; column < table.getColumnCount(); column++) {
    
            int width = 80;
    
            for (int row = 0; row < table.getRowCount(); row++) {
    
                TableCellRenderer renderer =
                        table.getCellRenderer(row, column);
    
                Component comp = table.prepareRenderer(
                        renderer, row, column);
    
                width = Math.max(
                        comp.getPreferredSize().width + 20,
                        width);
            }
    
            TableColumnModel columnModel =
                    table.getColumnModel();
    
            columnModel.getColumn(column)
                    .setPreferredWidth(width);
        }
    }
    // ─────────────────────────────────────────
    //  WRITE PDF
    // ─────────────────────────────────────────
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