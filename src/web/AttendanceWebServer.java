package src.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import src.db.DBConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AttendanceWebServer {

    // ── read server IP from system property set at QR-generate time ──
    public static String SERVER_IP = "192.168.1.9";

    public static void start() {
        try {
            HttpServer server =
                HttpServer.create(new InetSocketAddress(8080), 0);

            // ── GET /attendance?token=XXX ──
            server.createContext("/attendance", exchange -> {
                if (!"GET".equals(exchange.getRequestMethod())) return;

                Map<String, String> params =
                    parseQuery(exchange.getRequestURI().getQuery());
                String token = params.getOrDefault("token", "");

                // validate token exists and is active
                SessionInfo info = loadSession(token);
                String html;

                if (info == null) {
                    html = errorPage("Invalid or Expired QR Code",
                        "This QR code is no longer valid. " +
                        "Ask your teacher to generate a new one.");
                } else {
                    html = attendancePage(token, info);
                }

                sendHtml(exchange, html, 200);
            });

            // ── POST /submitAttendance ──
            server.createContext("/submitAttendance", exchange -> {
                if (!"POST".equals(exchange.getRequestMethod())) return;

                Map<String, String> form =
                    parseFormData(readBody(exchange));

                String token      = form.getOrDefault("token",      "");
                String studentId  = form.getOrDefault("studentId",  "");
                String studentName= form.getOrDefault("studentName","");
                String pin        = form.getOrDefault("pin",        "");
                String deviceFp   = form.getOrDefault("deviceFp",   "");
                String lat        = form.getOrDefault("lat",        "");
                String lon        = form.getOrDefault("lon",        "");

                String clientIp   = getClientIp(exchange);

                String html = processAttendance(
                    token, studentId, studentName,
                    pin, deviceFp, clientIp, lat, lon);

                sendHtml(exchange, html, 200);
            });

            server.start();
            System.out.println("Attendance Web Server started on port 8080");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────
    //  PROCESS ATTENDANCE — all validations
    // ─────────────────────────────────────────
    private static String processAttendance(
            String token,    String studentIdStr,
            String studentName, String pin,
            String deviceFp, String clientIp,
            String lat,      String lon) {

        try {
            Connection con = DBConnection.getConnection();

            // ── 1. Load session ──
            SessionInfo info = loadSession(token);
            if (info == null) {
                return errorPage("Session Expired",
                    "This QR session has expired or is invalid. " +
                    "Ask your teacher to regenerate the QR.");
            }

            // ── 2. PIN validation ──
            if (!info.pin.equals(pin.trim())) {
                return errorPage("Wrong PIN",
                    "The PIN you entered is incorrect. " +
                    "Please check with your teacher.");
            }

            // ── 3. WiFi / IP validation ──
            if (info.allowedIp != null && !info.allowedIp.isBlank()) {
                if (!clientIp.startsWith(info.allowedIp)) {
                    return errorPage("Wrong Network",
                        "You must be connected to the classroom WiFi " +
                        "to mark attendance. Your IP: " + clientIp +
                        ". Required subnet: " + info.allowedIp + ".x");
                }
            }

            // ── 4. Device fingerprint check ──
            if (deviceFp != null && !deviceFp.isBlank()) {
                PreparedStatement devCheck = con.prepareStatement(
                    "SELECT id FROM qr_device_log " +
                    "WHERE token=? AND device_fp=?");
                devCheck.setString(1, token);
                devCheck.setString(2, deviceFp);
                ResultSet devRs = devCheck.executeQuery();
                if (devRs.next()) {
                    return errorPage("Device Already Used",
                        "Attendance was already submitted from this device " +
                        "for this session. Contact your teacher if this is wrong.");
                }
            }

            // ── 5. Validate student ──
            int studentId;
            try {
                studentId = Integer.parseInt(studentIdStr.trim());
            } catch (NumberFormatException e) {
                return errorPage("Invalid Student ID",
                    "Please enter a valid numeric Student ID.");
            }

            PreparedStatement stuCheck = con.prepareStatement(
                "SELECT id, name FROM students WHERE id=? AND name=?");
            stuCheck.setInt(1, studentId);
            stuCheck.setString(2, studentName.trim());
            ResultSet stuRs = stuCheck.executeQuery();
            if (!stuRs.next()) {
                return errorPage("Student Not Found",
                    "No student found with ID " + studentId +
                    " and name '" + studentName + "'. " +
                    "Please check your details.");
            }

            // ── 6. Check enrollment ──
            PreparedStatement enrCheck = con.prepareStatement(
                "SELECT id FROM enrollments " +
                "WHERE student_id=? AND course_id=?");
            enrCheck.setInt(1, studentId);
            enrCheck.setInt(2, info.courseId);
            ResultSet enrRs = enrCheck.executeQuery();
            if (!enrRs.next()) {
                return errorPage("Not Enrolled",
                    "You are not enrolled in this course. " +
                    "Contact your teacher.");
            }

            // ── 7. Already marked? ──
            PreparedStatement attCheck = con.prepareStatement(
                "SELECT id FROM attendance " +
                "WHERE student_id=? AND course_id=? " +
                "AND attendance_date=?");
            attCheck.setInt(1, studentId);
            attCheck.setInt(2, info.courseId);
            attCheck.setString(3, info.attendanceDate);
            ResultSet attRs = attCheck.executeQuery();
            if (attRs.next()) {
                return warningPage("Already Marked",
                    "Your attendance for today has already been recorded.",
                    studentName);
            }

            // ── 8. Mark attendance ──
            PreparedStatement ins = con.prepareStatement(
                "INSERT INTO attendance " +
                "(student_id, course_id, attendance_date, status, remarks) " +
                "VALUES (?,?,?,'Present','QR Web Scan')");
            ins.setInt(1, studentId);
            ins.setInt(2, info.courseId);
            ins.setString(3, info.attendanceDate);
            ins.executeUpdate();

            // ── 9. Log device fingerprint ──
            if (deviceFp != null && !deviceFp.isBlank()) {
                try {
                    PreparedStatement devLog = con.prepareStatement(
                        "INSERT IGNORE INTO qr_device_log " +
                        "(token, student_id, device_fp, ip_address) " +
                        "VALUES (?,?,?,?)");
                    devLog.setString(1, token);
                    devLog.setInt(2, studentId);
                    devLog.setString(3, deviceFp);
                    devLog.setString(4, clientIp);
                    devLog.executeUpdate();
                } catch (Exception ignored) {}
            }

            return successPage(studentName, info.courseName,
                info.attendanceDate);

        } catch (Exception e) {
            e.printStackTrace();
            return errorPage("Server Error", e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    //  LOAD SESSION FROM DB
    // ─────────────────────────────────────────
    private static SessionInfo loadSession(String token) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT qs.token, qs.course_id, c.course_name, " +
                "qs.attendance_date, qs.pin, qs.allowed_ip, " +
                "qs.expiry_time, qs.active " +
                "FROM qr_sessions qs " +
                "JOIN courses c ON c.id = qs.course_id " +
                "WHERE qs.token=? AND qs.active=1 " +
                "AND qs.expiry_time > NOW()");
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            SessionInfo info      = new SessionInfo();
            info.token            = rs.getString("token");
            info.courseId         = rs.getInt("course_id");
            info.courseName       = rs.getString("course_name");
            info.attendanceDate   = rs.getString("attendance_date");
            info.pin              = rs.getString("pin");
            info.allowedIp        = rs.getString("allowed_ip");
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────
    //  HTML PAGES
    // ─────────────────────────────────────────
    private static String attendancePage(String token, SessionInfo info) {
        return "<!DOCTYPE html><html><head>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Mark Attendance</title>" +
            "<style>" +
            "* { box-sizing:border-box; margin:0; padding:0; }" +
            "body { font-family:Segoe UI,sans-serif; background:#f5f7fa;" +
            "  display:flex; justify-content:center; " +
            "  align-items:flex-start; min-height:100vh; padding:20px; }" +
            ".card { background:#fff; border-radius:12px; padding:28px 24px;" +
            "  width:100%; max-width:420px;" +
            "  box-shadow:0 2px 12px rgba(0,0,0,0.10); }" +
            "h2 { color:#0066cc; font-size:20px; margin-bottom:4px; }" +
            ".sub { color:#888; font-size:13px; margin-bottom:20px; }" +
            "label { font-size:13px; color:#444; font-weight:500;" +
            "  display:block; margin-bottom:4px; margin-top:14px; }" +
            "input[type=text], input[type=number], input[type=password] {" +
            "  width:100%; padding:10px 12px; border:1.5px solid #dde1e7;" +
            "  border-radius:7px; font-size:14px; outline:none;" +
            "  transition:border 0.2s; }" +
            "input:focus { border-color:#0066cc; }" +
            ".pin-row { display:flex; gap:8px; }" +
            ".pin-row input { text-align:center; font-size:22px;" +
            "  letter-spacing:4px; font-weight:bold; }" +
            "button { width:100%; padding:13px; background:#0066cc;" +
            "  color:#fff; border:none; border-radius:8px;" +
            "  font-size:15px; font-weight:600; margin-top:20px;" +
            "  cursor:pointer; transition:background 0.2s; }" +
            "button:hover { background:#0052a3; }" +
            ".badge { display:inline-block; background:#e8f0fe;" +
            "  color:#0066cc; border-radius:6px; padding:4px 10px;" +
            "  font-size:12px; font-weight:600; margin-bottom:16px; }" +
            ".info { background:#f0faf5; border-left:3px solid #27a744;" +
            "  padding:10px 14px; border-radius:0 6px 6px 0;" +
            "  font-size:12px; color:#444; margin-top:14px; }" +
            ".gps-row { display:flex; align-items:center; gap:8px;" +
            "  margin-top:10px; font-size:12px; color:#888; }" +
            "#gpsStatus { font-weight:600; }" +
            "</style>" +
            "</head><body><div class='card'>" +
            "<h2>📋 Mark Attendance</h2>" +
            "<p class='sub'>Fill in your details below</p>" +
            "<span class='badge'>📚 " + info.courseName + "</span><br>" +
            "<span class='badge'>📅 " + info.attendanceDate + "</span>" +

            "<form id='attForm' action='/submitAttendance' method='POST'" +
            "  onsubmit='return collectAndSubmit(event)'>" +

            "<input type='hidden' name='token' value='" + token + "'>" +
            "<input type='hidden' name='deviceFp' id='deviceFp'>" +
            "<input type='hidden' name='lat' id='latInput'>" +
            "<input type='hidden' name='lon' id='lonInput'>" +

            "<label>Student ID *</label>" +
            "<input type='number' name='studentId' placeholder='Enter your student ID'" +
            "  required min='1'>" +

            "<label>Full Name *</label>" +
            "<input type='text' name='studentName'" +
            "  placeholder='Enter your full name as registered'" +
            "  required autocomplete='name'>" +

            "<label>Session PIN *</label>" +
            "<input type='password' name='pin' placeholder='Enter 4-digit PIN'" +
            "  maxlength='4' required autocomplete='off'" +
            "  style='letter-spacing:6px;font-size:20px;font-weight:bold'>" +

            "<div class='gps-row'>📍 Location: " +
            "<span id='gpsStatus'>Detecting...</span></div>" +

            "<div class='info'>Make sure you are connected to the " +
            "<b>classroom WiFi</b> before submitting.</div>" +

            "<button type='submit'>✅ Mark My Attendance</button>" +
            "</form>" +

            "<script>" +
            // device fingerprint
            "function getFingerprint() {" +
            "  var fp = localStorage.getItem('_att_fp');" +
            "  if (!fp) {" +
            "    fp = Math.random().toString(36).substr(2,9) + '_'" +
            "       + Date.now().toString(36);" +
            "    localStorage.setItem('_att_fp', fp);" +
            "  }" +
            "  return fp;" +
            "}" +
            "document.getElementById('deviceFp').value = getFingerprint();" +

            // GPS
            "if (navigator.geolocation) {" +
            "  navigator.geolocation.getCurrentPosition(function(pos) {" +
            "    document.getElementById('latInput').value = pos.coords.latitude;" +
            "    document.getElementById('lonInput').value = pos.coords.longitude;" +
            "    document.getElementById('gpsStatus').textContent =" +
            "      '✓ Got (' + pos.coords.latitude.toFixed(4) + ',' +" +
            "       pos.coords.longitude.toFixed(4) + ')';" +
            "    document.getElementById('gpsStatus').style.color='#27a744';" +
            "  }, function() {" +
            "    document.getElementById('gpsStatus').textContent='Not available';" +
            "  });" +
            "} else {" +
            "  document.getElementById('gpsStatus').textContent='Not supported';" +
            "}" +

            // submit
            "function collectAndSubmit(e) {" +
            "  document.getElementById('deviceFp').value = getFingerprint();" +
            "  return true;" +
            "}" +
            "</script>" +
            "</div></body></html>";
    }

    private static String successPage(String name,
                                       String course,
                                       String date) {
        return "<!DOCTYPE html><html><head>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Attendance Marked</title>" +
            "<style>" +
            "body { font-family:Segoe UI,sans-serif; background:#f0faf5;" +
            "  display:flex; justify-content:center; align-items:center;" +
            "  min-height:100vh; }" +
            ".card { background:#fff; border-radius:12px; padding:36px 28px;" +
            "  text-align:center; max-width:380px; width:100%;" +
            "  box-shadow:0 2px 12px rgba(0,0,0,0.10); }" +
            ".icon { font-size:60px; margin-bottom:12px; }" +
            "h2 { color:#27a744; font-size:22px; margin-bottom:8px; }" +
            "p { color:#555; font-size:14px; line-height:1.7; }" +
            ".pill { display:inline-block; background:#e8f5e9;" +
            "  color:#27a744; border-radius:20px; padding:4px 14px;" +
            "  font-size:12px; font-weight:600; margin-top:10px; }" +
            "</style></head><body><div class='card'>" +
            "<div class='icon'>✅</div>" +
            "<h2>Attendance Marked!</h2>" +
            "<p>Hi <b>" + name + "</b>,<br>" +
            "Your attendance for <b>" + course + "</b><br>" +
            "on <b>" + date + "</b> has been recorded.</p>" +
            "<div class='pill'>Status: Present</div>" +
            "</div></body></html>";
    }

    private static String warningPage(String title,
                                       String msg,
                                       String name) {
        return "<!DOCTYPE html><html><head>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>" + title + "</title>" +
            "<style>" +
            "body { font-family:Segoe UI,sans-serif; background:#fffbf0;" +
            "  display:flex; justify-content:center; align-items:center;" +
            "  min-height:100vh; }" +
            ".card { background:#fff; border-radius:12px; padding:36px 28px;" +
            "  text-align:center; max-width:380px; width:100%;" +
            "  box-shadow:0 2px 12px rgba(0,0,0,0.10); }" +
            ".icon { font-size:60px; margin-bottom:12px; }" +
            "h2 { color:#e07000; font-size:20px; margin-bottom:8px; }" +
            "p { color:#555; font-size:14px; }" +
            "</style></head><body><div class='card'>" +
            "<div class='icon'>⚠️</div>" +
            "<h2>" + title + "</h2>" +
            "<p>Hi <b>" + name + "</b>, " + msg + "</p>" +
            "</div></body></html>";
    }

    private static String errorPage(String title, String msg) {
        return "<!DOCTYPE html><html><head>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Error</title>" +
            "<style>" +
            "body { font-family:Segoe UI,sans-serif; background:#fff5f5;" +
            "  display:flex; justify-content:center; align-items:center;" +
            "  min-height:100vh; }" +
            ".card { background:#fff; border-radius:12px; padding:36px 28px;" +
            "  text-align:center; max-width:380px; width:100%;" +
            "  box-shadow:0 2px 12px rgba(0,0,0,0.10); }" +
            ".icon { font-size:60px; margin-bottom:12px; }" +
            "h2 { color:#dc3545; font-size:20px; margin-bottom:8px; }" +
            "p { color:#555; font-size:14px; }" +
            "</style></head><body><div class='card'>" +
            "<div class='icon'>❌</div>" +
            "<h2>" + title + "</h2>" +
            "<p>" + msg + "</p>" +
            "</div></body></html>";
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────
    private static void sendHtml(HttpExchange ex,
                                  String html, int code)
            throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type",
            "text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange ex)
            throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(),
                StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(
                        URLDecoder.decode(kv[0], "UTF-8"),
                        URLDecoder.decode(kv[1], "UTF-8"));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static Map<String, String> parseFormData(String body) {
        return parseQuery(body);
    }

    private static String getClientIp(HttpExchange ex) {
        String forwarded = ex.getRequestHeaders()
            .getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();
        return ex.getRemoteAddress().getAddress()
            .getHostAddress();
    }

    // ─────────────────────────────────────────
    //  SESSION INFO DTO
    // ─────────────────────────────────────────
    static class SessionInfo {
        String token;
        int    courseId;
        String courseName;
        String attendanceDate;
        String pin;
        String allowedIp;
    }
}