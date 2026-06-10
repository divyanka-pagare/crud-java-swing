package src.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
// import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
// import com.google.zxing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    // ── Generate QR as BufferedImage ──
    public static BufferedImage generate(String content,
                                          int width,
                                          int height) throws WriterException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix  = writer.encode(
            content, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage img = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y,
                    matrix.get(x, y)
                        ? Color.BLACK.getRGB()
                        : Color.WHITE.getRGB());
            }
        }
        return img;
    }

    // ── Build QR content string ──
    // Format: ATT|courseId|courseName|date|sessionToken
    public static String buildContent(int courseId,
                                       String courseName,
                                       String date,
                                       String token) {
        return "ATT|" + courseId + "|" + courseName
             + "|" + date + "|" + token;
    }

    // ── Parse QR content string ──
    public static String[] parseContent(String content) {
        if (content == null || !content.startsWith("ATT|"))
            return null;
        String[] parts = content.split("\\|");
        return parts.length == 5 ? parts : null;
    }

    // ── Generate a random session token ──
    public static String generateToken() {
        return Long.toHexString(System.currentTimeMillis())
             + Integer.toHexString((int)(Math.random() * 0xFFFF));
    }
}