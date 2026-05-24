package src.forms.transaction;

import src.models.Student;
import src.services.PaymentService;
import src.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;


public class UpiPaymentScreen extends JFrame {

    static final int TIMER_SECONDS = 180; // 3 minutes

    int remainingSeconds = TIMER_SECONDS;
    Timer countdownTimer;

    JLabel lblTimer;
    JLabel lblStatus;
    JPanel qrPanel;
    JPanel successPanel;
    JButton downloadBtn;
    JButton goBackBtn;
    JPanel bottomPanel;

    Student student;
    double  amountPayable;
    FeesReceiptForm parentForm;

    // data passed for receipt
    double total, disc;
    String mode, txnId;
    int    courseCount;

    public UpiPaymentScreen(FeesReceiptForm parent,
                             Student s,
                             double total,
                             double disc,
                             double payable,
                             int courseCount) {

        this.parentForm    = parent;
        this.student       = s;
        this.total         = total;
        this.disc          = disc;
        this.amountPayable = payable;
        this.courseCount   = courseCount;
        this.mode          = "Online (UPI)";
        this.txnId         = "UPI" + System.currentTimeMillis() % 100000;

        setTitle("UPI Payment");
        setSize(520, 720);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel main = new JPanel(null);
        main.setBackground(new Color(245, 247, 250));

        // ── Title ──
        JLabel title = new JLabel("Pay via UPI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBounds(0, 18, 520, 36);
        main.add(title);

        // ── Amount ──
        JLabel lblAmt = new JLabel(
            String.format("Amount Payable :  ₹ %,.2f", amountPayable));
        lblAmt.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAmt.setForeground(new Color(0, 102, 204));
        lblAmt.setHorizontalAlignment(JLabel.CENTER);
        lblAmt.setBounds(0, 60, 520, 28);
        main.add(lblAmt);

        // ── Student ──
        JLabel lblStu = new JLabel("Student: " + student.getName());
        lblStu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStu.setForeground(new Color(80, 80, 90));
        lblStu.setHorizontalAlignment(JLabel.CENTER);
        lblStu.setBounds(0, 90, 520, 22);
        main.add(lblStu);

        // ── UPI Options ──
        JPanel upiPanel = new JPanel(null);
        upiPanel.setBackground(Color.WHITE);
        upiPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210,215,220)),
            "Choose UPI App",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        upiPanel.setBounds(30, 122, 455, 70);
        main.add(upiPanel);

        // UPI app buttons
        String[] upiApps = {"GPay", "PhonePe", "Paytm", "BHIM"};
        Color[]  upiColors = {
            new Color(66, 133, 244),   // GPay blue
            new Color(83, 0, 195),     // PhonePe purple
            new Color(0, 164, 219),    // Paytm blue
            new Color(0, 122, 255)     // BHIM blue
        };

        ButtonGroup upiGroup = new ButtonGroup();
        int btnX = 15;

        for (int i = 0; i < upiApps.length; i++) {
            final Color  appColor = upiColors[i];

            JToggleButton tb = new JToggleButton(upiApps[i]);
            tb.setFont(new Font("Segoe UI", Font.BOLD, 12));
            tb.setBackground(i == 0 ? upiColors[0] : new Color(240,240,240));
            tb.setForeground(i == 0 ? Color.WHITE : Color.DARK_GRAY);
            tb.setBounds(btnX, 28, 95, 30);
            tb.setFocusPainted(false);
            tb.setBorderPainted(false);

            if (i == 0) tb.setSelected(true);

            tb.addActionListener(e -> {
                // reset all
                for (Component c : upiPanel.getComponents()) {
                    if (c instanceof JToggleButton) {
                        c.setBackground(new Color(240,240,240));
                        ((JButton)c).setForeground(Color.DARK_GRAY);
                    }
                }
                tb.setBackground(appColor);
                tb.setForeground(Color.WHITE);
            });

            upiGroup.add(tb);
            upiPanel.add(tb);
            btnX += 105;
        }

        // ── QR Code Panel ──
        qrPanel = new JPanel(null);
        qrPanel.setBackground(Color.WHITE);
        qrPanel.setBorder(BorderFactory.createLineBorder(
            new Color(210,215,220)));
        qrPanel.setBounds(30, 202, 455, 310);
        main.add(qrPanel);

        JLabel lblScan = new JLabel("Scan QR Code to Pay");
        lblScan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblScan.setHorizontalAlignment(JLabel.CENTER);
        lblScan.setBounds(0, 12, 455, 22);
        qrPanel.add(lblScan);

        // QR Code drawing (simulated GPay-style QR)
        JPanel qrDraw = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                drawQRCode(g2, getWidth(), getHeight());
            }
        };
        qrDraw.setBackground(Color.WHITE);
        qrDraw.setBounds(127, 44, 200, 200);
        qrPanel.add(qrDraw);

        // UPI ID label
        JLabel lblUpiId = new JLabel("UPI ID: studentmgmt@upi");
        lblUpiId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUpiId.setForeground(new Color(80,80,90));
        lblUpiId.setHorizontalAlignment(JLabel.CENTER);
        lblUpiId.setBounds(0, 252, 455, 20);
        qrPanel.add(lblUpiId);

        // Timer label
        lblTimer = new JLabel("QR expires in: 03:00");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTimer.setForeground(new Color(220, 53, 69));
        lblTimer.setHorizontalAlignment(JLabel.CENTER);
        lblTimer.setBounds(0, 276, 455, 22);
        qrPanel.add(lblTimer);

        // ── Status Label ──
        lblStatus = new JLabel("Waiting for payment...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(new Color(100,100,110));
        lblStatus.setHorizontalAlignment(JLabel.CENTER);
        lblStatus.setBounds(0, 522, 520, 24);
        main.add(lblStatus);

        // ── Success Panel (hidden initially) ──
        successPanel = new JPanel(null);
        successPanel.setBackground(new Color(245, 247, 250));
        successPanel.setBounds(30, 202, 455, 310);
        successPanel.setVisible(false);
        main.add(successPanel);

        // Green checkmark
        JPanel checkPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Green circle
                g2.setColor(new Color(40, 167, 69));
                g2.fillOval(10, 10, 100, 100);
                // White checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(8,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(28, 62, 50, 84);
                g2.drawLine(50, 84, 92, 36);
            }
        };
        checkPanel.setBackground(new Color(245, 247, 250));
        checkPanel.setBounds(167, 30, 120, 120);
        successPanel.add(checkPanel);

        JLabel lblSuccessTitle = new JLabel("Payment Successful!");
        lblSuccessTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSuccessTitle.setForeground(new Color(40, 167, 69));
        lblSuccessTitle.setHorizontalAlignment(JLabel.CENTER);
        lblSuccessTitle.setBounds(0, 160, 455, 30);
        successPanel.add(lblSuccessTitle);

        JLabel lblSuccessAmt = new JLabel(
            String.format("₹ %,.2f paid successfully", amountPayable));
        lblSuccessAmt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSuccessAmt.setForeground(new Color(50, 50, 50));
        lblSuccessAmt.setHorizontalAlignment(JLabel.CENTER);
        lblSuccessAmt.setBounds(0, 196, 455, 24);
        successPanel.add(lblSuccessAmt);

        JLabel lblTxn = new JLabel("Transaction ID: " + txnId);
        lblTxn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTxn.setForeground(new Color(100, 100, 110));
        lblTxn.setHorizontalAlignment(JLabel.CENTER);
        lblTxn.setBounds(0, 224, 455, 20);
        successPanel.add(lblTxn);

        // ── Bottom Buttons (hidden until success) ──
        bottomPanel = new JPanel(null);
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBounds(30, 560, 455, 100);
        bottomPanel.setVisible(false);
        main.add(bottomPanel);

        downloadBtn = new JButton("Download Fee Receipt");
        downloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        downloadBtn.setBackground(new Color(40, 167, 69));
        downloadBtn.setForeground(Color.WHITE);
        downloadBtn.setBounds(0, 0, 455, 42);
        downloadBtn.setFocusPainted(false);
        bottomPanel.add(downloadBtn);

        goBackBtn = new JButton("Go Back");
        goBackBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        goBackBtn.setBackground(new Color(108, 117, 125));
        goBackBtn.setForeground(Color.WHITE);
        goBackBtn.setBounds(0, 50, 455, 42);
        goBackBtn.setFocusPainted(false);
        bottomPanel.add(goBackBtn);

        // ── Simulate Payment Button (for testing) ──
        JButton simulateBtn = new JButton("Simulate Payment Done ✓");
        simulateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        simulateBtn.setBackground(new Color(255, 193, 7));
        simulateBtn.setForeground(Color.BLACK);
        simulateBtn.setBounds(140, 528, 230, 30);
        simulateBtn.setFocusPainted(false);
        main.add(simulateBtn);

        add(main);

        // ── Start countdown ──
        startCountdown();

        // ── Listeners ──
        simulateBtn.addActionListener(e -> onPaymentSuccess());

        downloadBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Payment Successful!"
            );
            
            parentForm.dispose();
            
            new FeesReceiptForm().setVisible(true);
            
            dispose();
        });

        goBackBtn.addActionListener(e -> {
            stopCountdown();
            dispose();
            UIUtils.openFullScreen(parentForm);
            parentForm.setVisible(true);
            parentForm.loadReceiptTable(null);
            parentForm.populateFilterDropdown();
            parentForm.onStudentSelected();
            // ===== SHOW NOTIFICATION ONLY HERE =====
            JOptionPane.showMessageDialog(parentForm,
                "Payment of ₹" + String.format("%,.2f", amountPayable) +
                " completed successfully!\n" +
                "Transaction ID : " + txnId + "\n" +
                "Mode           : " + mode + "\n\n" +
                "You can download the receipt anytime from the form.",
                "Payment Confirmed",
                JOptionPane.INFORMATION_MESSAGE);
        });

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  COUNTDOWN TIMER
    // ─────────────────────────────────────────
    private void startCountdown() {
        countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    remainingSeconds--;

                    if (remainingSeconds <= 0) {
                        stopCountdown();
                        onQRExpired();
                        return;
                    }

                    int mins = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    lblTimer.setText(String.format(
                        "QR expires in: %02d:%02d", mins, secs));

                    // Turn orange in last 30 seconds
                    if (remainingSeconds <= 30) {
                        lblTimer.setForeground(new Color(255, 140, 0));
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    // ─────────────────────────────────────────
    //  QR EXPIRED
    // ─────────────────────────────────────────
    private void onQRExpired() {
        lblTimer.setText("QR Expired");
        lblTimer.setForeground(new Color(220, 53, 69));
        lblStatus.setText("QR code expired. Please go back and try again.");

        int choice = JOptionPane.showConfirmDialog(this,
            "QR code has expired.\nDo you want to go back?",
            "QR Expired",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            parentForm.setVisible(true);
        }
    }

    // ─────────────────────────────────────────
    //  PAYMENT SUCCESS
    // ─────────────────────────────────────────
    private void onPaymentSuccess() {
        stopCountdown();

        PaymentService paymentService =
        new PaymentService(parentForm.con);

        paymentService.savePaymentToDB(
                student,
                total,
                disc,
                amountPayable,
                "Online",
                courseCount,
                parentForm.filteredCourseIds,
                parentForm.courseTableModel
        );

        // Hide QR, show success
        qrPanel    .setVisible(false);
        successPanel.setVisible(true);
        bottomPanel .setVisible(true);
        lblStatus  .setText("Payment completed successfully!");
        lblStatus  .setForeground(new Color(40, 167, 69));
    }

    // ─────────────────────────────────────────
    //  DRAW SIMULATED QR CODE
    // ─────────────────────────────────────────
    private void drawQRCode(Graphics2D g2, int W, int H) {

        int cell = 6;
        int cols = W / cell;
        int rows = H / cell;

        // Use deterministic pattern based on student id
        long seed = student.getId() * 123456789L + (long)(amountPayable * 100);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, W, H);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // finder patterns (3 corners)
                if (isFinderPattern(r, c, rows, cols)) {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(c * cell, r * cell, cell, cell);
                    continue;
                }
                // data modules
                long hash = (seed ^ (r * 31L + c)) * 2654435761L;
                if ((hash & 1) == 1) {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(c * cell, r * cell, cell, cell);
                }
            }
        }

        // Draw finder square borders
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        // top-left
        g2.drawRect(0, 0, 6*cell, 6*cell);
        g2.fillRect(cell, cell, 4*cell, 4*cell);
        g2.setColor(Color.WHITE);
        g2.fillRect(2*cell, 2*cell, 2*cell, 2*cell);
        // top-right
        g2.setColor(Color.BLACK);
        g2.drawRect(W-7*cell, 0, 6*cell, 6*cell);
        g2.fillRect(W-6*cell, cell, 4*cell, 4*cell);
        g2.setColor(Color.WHITE);
        g2.fillRect(W-4*cell, 2*cell, 2*cell, 2*cell);
        // bottom-left
        g2.setColor(Color.BLACK);
        g2.drawRect(0, H-7*cell, 6*cell, 6*cell);
        g2.fillRect(cell, H-6*cell, 4*cell, 4*cell);
        g2.setColor(Color.WHITE);
        g2.fillRect(2*cell, H-4*cell, 2*cell, 2*cell);
    }

    private boolean isFinderPattern(int r, int c, int rows, int cols) {
        int fp = 7;
        return (r < fp && c < fp) ||
               (r < fp && c >= cols - fp) ||
               (r >= rows - fp && c < fp);
    }
}