package src.utils;

import src.models.Student;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFReceiptGenerator {

    public static void generateReceipt(
            Component parent,
            Student s,
            double total,
            double disc,
            double payable,
            String mode,
            String txnId,
            DefaultTableModel courseTableModel
    ) {

        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Save Fee Receipt as PDF");

        fc.setSelectedFile(
                new File(
                        s.getName().replace(" ", "_")
                                + "_FeeReceipt.pdf"
                )
        );

        if (fc.showSaveDialog(parent)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File pdfFile = fc.getSelectedFile();

        if (!pdfFile.getName().endsWith(".pdf")) {

            pdfFile = new File(
                    pdfFile.getAbsolutePath() + ".pdf"
            );
        }

        try {

            int W = 595 * 2;
            int H = 842 * 2;

            BufferedImage img =
                    new BufferedImage(
                            W,
                            H,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D g = img.createGraphics();

            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g.scale(2.0, 2.0);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            int x = 40;
            int y = 40;
            int lineH = 22;

            // HEADER
            g.setColor(new Color(0, 102, 204));
            g.fillRect(0, 0, 595, 70);

            g.setColor(Color.WHITE);

            g.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g.drawString("FEES RECEIPT", x, 38);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("Student Management System", x, 55);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            g.drawString(
                    "Date: "
                            + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy HH:mm:ss"
                            )
                    ),
                    400,
                    55
            );

            y = 90;

            // RECEIPT NUMBER
            g.setColor(new Color(50, 50, 50));

            g.setFont(new Font("Segoe UI", Font.BOLD, 11));

            g.drawString(
                    "Receipt No : REC-"
                            + s.getId()
                            + "-"
                            + System.currentTimeMillis() % 10000,
                    x,
                    y
            );

            y += lineH;

            // STUDENT DETAILS
            g.drawString(
                    "Student    : " + s.getName(),
                    x,
                    y
            );

            y += lineH;

            g.drawString(
                    "Email      : " + s.getEmail(),
                    x,
                    y
            );

            y += lineH;

            g.drawString(
                    "Phone      : " + s.getPhone(),
                    x,
                    y
            );

            y += lineH;

            g.drawString(
                    "Gender     : " + s.getGender(),
                    x,
                    y
            );

            y += lineH + 8;

            // DIVIDER
            g.setColor(new Color(200, 200, 200));

            g.drawLine(x, y, 555, y);

            y += 14;

            // TABLE HEADER
            g.setColor(new Color(0, 102, 204));

            g.fillRect(x - 5, y - 14, 520, 22);

            g.setColor(Color.WHITE);

            g.setFont(new Font("Segoe UI", Font.BOLD, 11));

            g.drawString("Course Name", x, y);
            g.drawString("Duration", x + 250, y);
            g.drawString("Fees (INR)", x + 390, y);

            y += lineH;

            // COURSE ROWS
            boolean alt = false;

            for (int i = 0; i < courseTableModel.getRowCount(); i++) {

                String cName =
                        courseTableModel.getValueAt(i, 0).toString();

                String cDur =
                        courseTableModel.getValueAt(i, 1).toString();

                String cFee =
                        courseTableModel.getValueAt(i, 2)
                                .toString()
                                .replace("₹", "")
                                .replace(",", "")
                                .trim();

                if (alt) {

                    g.setColor(new Color(245, 248, 255));

                    g.fillRect(x - 5, y - 14, 520, 20);
                }

                alt = !alt;

                g.setColor(new Color(50, 50, 50));

                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                g.drawString(cName, x, y);
                g.drawString(cDur, x + 250, y);

                g.drawString(
                        String.format(
                                "%.2f",
                                Double.parseDouble(cFee)
                        ),
                        x + 390,
                        y
                );

                y += lineH;
            }

            // DIVIDER
            y += 6;

            g.setColor(new Color(200, 200, 200));

            g.drawLine(x, y, 555, y);

            y += 16;

            // FEES SUMMARY
            g.setColor(new Color(50, 50, 50));

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            g.drawString(
                    String.format(
                            "Course Total    :  INR %.2f",
                            total
                    ),
                    x,
                    y
            );

            y += lineH;

            if (disc > 0) {

                g.setColor(new Color(40, 167, 69));

                g.drawString(
                        String.format(
                                "Discount (10%%) :  - INR %.2f",
                                disc
                        ),
                        x,
                        y
                );

                y += lineH;
            }

            g.setColor(new Color(0, 102, 204));

            g.setFont(new Font("Segoe UI", Font.BOLD, 14));

            g.drawString(
                    String.format(
                            "Amount Paid     :  INR %.2f",
                            payable
                    ),
                    x,
                    y
            );

            y += lineH + 8;

            // PAYMENT DETAILS
            g.setColor(new Color(50, 50, 50));

            g.setFont(new Font("Segoe UI", Font.BOLD, 12));

            g.drawString(
                    "Payment Mode    :  " + mode,
                    x,
                    y
            );

            y += lineH;

            if (!txnId.isEmpty()) {

                g.drawString(
                        "Transaction ID  :  " + txnId,
                        x,
                        y
                );

                y += lineH;
            }

            // PAID BADGE
            y += 6;

            g.setColor(new Color(40, 167, 69));

            g.fillRoundRect(x, y, 80, 24, 8, 8);

            g.setColor(Color.WHITE);

            g.setFont(new Font("Segoe UI", Font.BOLD, 12));

            g.drawString("PAID", x + 22, y + 16);

            y += 40;

            // FOOTER
            g.setColor(new Color(200, 200, 200));

            g.drawLine(x, y, 555, y);

            y += 14;

            g.setFont(new Font("Segoe UI", Font.ITALIC, 10));

            g.setColor(new Color(140, 140, 140));

            g.drawString(
                    "Thank you for your payment. Keep learning and growing!",
                    x,
                    y
            );

            g.dispose();

            FileOutputStream fos =
                    new FileOutputStream(pdfFile);

            writePDF(fos, img, W, H);

            fos.close();

            JOptionPane.showMessageDialog(
                    parent,
                    "Receipt saved!\n"
                            + pdfFile.getAbsolutePath()
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Error: " + ex.getMessage()
            );
        }
    }

    private static void writePDF(
            OutputStream out,
            BufferedImage img,
            int W,
            int H
    ) throws Exception {

        ByteArrayOutputStream jpegOut =
                new ByteArrayOutputStream();

        ImageIO.write(img, "jpeg", jpegOut);

        byte[] imgBytes = jpegOut.toByteArray();

        String w = String.valueOf(W);
        String h = String.valueOf(H);

        String obj1 =
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";

        String obj2 =
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";

        String obj3 =
                "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                        + "/MediaBox [0 0 "
                        + w
                        + " "
                        + h
                        + "] "
                        + "/Contents 4 0 R /Resources "
                        + "<< /XObject << /Im1 5 0 R >> >> >>\nendobj\n";

        String obj4body =
                "q "
                        + w
                        + " 0 0 "
                        + h
                        + " 0 0 cm /Im1 Do Q\n";

        String obj4 =
                "4 0 obj\n<< /Length "
                        + obj4body.length()
                        + " >>\nstream\n"
                        + obj4body
                        + "endstream\nendobj\n";

        String obj5h =
                "5 0 obj\n<< /Type /XObject /Subtype /Image "
                        + "/Width "
                        + w
                        + " /Height "
                        + h
                        + " /ColorSpace /DeviceRGB /BitsPerComponent 8 "
                        + "/Filter /DCTDecode /Length "
                        + imgBytes.length
                        + " >>\nstream\n";

        String obj5f =
                "\nendstream\nendobj\n";

        String header = "%PDF-1.4\n";

        int off1 = header.length();
        int off2 = off1 + obj1.length();
        int off3 = off2 + obj2.length();
        int off4 = off3 + obj3.length();
        int off5 = off4 + obj4.length();

        String xref =
                "xref\n0 6\n"
                        + "0000000000 65535 f \n"
                        + String.format("%010d 00000 n \n", off1)
                        + String.format("%010d 00000 n \n", off2)
                        + String.format("%010d 00000 n \n", off3)
                        + String.format("%010d 00000 n \n", off4)
                        + String.format("%010d 00000 n \n", off5);

        int startxref =
                off5
                        + obj5h.length()
                        + imgBytes.length
                        + obj5f.length();

        String trailer =
                "trailer\n<< /Size 6 /Root 1 0 R >>\n"
                        + "startxref\n"
                        + startxref
                        + "\n%%EOF\n";

        PrintStream ps =
                new PrintStream(out, true, "UTF-8");

        ps.print(header);
        ps.print(obj1);
        ps.print(obj2);
        ps.print(obj3);
        ps.print(obj4);
        ps.print(obj5h);

        out.write(imgBytes);

        ps.print(obj5f);
        ps.print(xref);
        ps.print(trailer);

        ps.flush();
    }
}